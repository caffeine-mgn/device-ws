package pw.binom

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.protobuf.ProtoBuf
import pw.binom.concurrency.SpinLock
import pw.binom.concurrency.synchronize
import pw.binom.device.ws.dto.DeviceMessage
import pw.binom.device.ws.dto.ServerMessage
import pw.binom.io.http.websocket.MessageType
import pw.binom.io.http.websocket.WebSocketClosedException
import pw.binom.io.http.websocket.WebSocketConnection
import pw.binom.io.readBytes
import pw.binom.io.useAsync
import pw.binom.logger.DEBUG
import pw.binom.logger.INFO
import pw.binom.logger.Logger
import pw.binom.logger.SEVERE
import pw.binom.logger.WARNING
import pw.binom.mq.MapHeaders
import pw.binom.mq.nats.NatsMqConnection
import pw.binom.network.NetworkManager
import pw.binom.tracing.zipkin.ZipkinTracing
import pw.binom.traycing.strong.ZipkinCollector
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume
import kotlin.time.Duration
import kotlin.time.measureTime

class DeviceControl(
    val id: String,
    val name: String,
    private val connection: WebSocketConnection,
    private val networkManager: NetworkManager,
    private val nats: NatsMqConnection,
    private val messageContentType: String,
    private val topicPrefix: String,
    private val pingInterval: Duration,
    private val pingTimeout: Duration,
    private val zipkinCollector: ZipkinCollector,
) {
    private val logger = Logger.getLogger("Device $id $name")
    private var functions = emptyMap<String, String>()
    private var events = emptyMap<String, String>()

    private val sender = ProcessingChannel<ServerMessage> { input ->
        connection.write(MessageType.BINARY).useAsync { output ->
            val data = ProtoBuf.encodeToByteArray(ServerMessage.serializer(), input)
            output.writeInt(data.size)
            output.write(data)
        }
    }

    suspend fun income(message: ServerMessage) {
        sender.push(message)
    }

    private val pingWaiters = HashMap<Int, CancellableContinuation<Unit>>()
    private val pingLock = SpinLock()

    private var pingIterator = 0

    suspend fun ping() {
        val id = pingIterator++
        sender.push(ServerMessage.Ping(id))
        suspendCancellableCoroutine<Unit> {
            it.invokeOnCancellation {
                pingLock.synchronize {
                    pingWaiters.remove(id)
                }
            }
            pingLock.synchronize {
                pingWaiters[id] = it
            }
        }
    }

    var ping: Duration? = null
        private set

    suspend fun processing() {
        sender.start(networkManager)
        if (pingInterval.isPositive()) {
            networkManager.launch {
                while (isActive) {
                    ping = withTimeoutOrNull(pingTimeout) {
                        measureTime {
                            ping()
                        }
                    }
                    delay(pingInterval)
                }
            }
        }
        val controlListener = ControlListener.create(
            topicName = "$topicPrefix.$id.rpc",
            nats = nats,
            incomeMessages = this::income,
        )
        try {
            while (coroutineContext.isActive) {
                connection.read().useAsync { input ->
                    val size = input.readInt()
                    val deviceMessage = ProtoBuf.decodeFromByteArray(
                        DeviceMessage.serializer(),
                        input.readBytes(size),
                    )
                    when (deviceMessage) {
                        is DeviceMessage.Event -> nats.producer("$topicPrefix.$id.events") {
                            val headers = buildMap {
                                deviceMessage.traceId?.let { put("trace-id", listOf(it)) }
                                deviceMessage.spanId?.let { put("span-id", listOf(it)) }
                                put("content-type", listOf(messageContentType))
                            }
                            send(
                                headers = MapHeaders(headers),
                                data = deviceMessage.data,
                            )
                        }

                        is DeviceMessage.RPCResponse -> nats.producer(deviceMessage.id) {
                            send(
                                headers = MapHeaders(
                                    mapOf(
                                        "content-type" to listOf(messageContentType),
                                        "status" to listOf("ok"),
                                    )
                                ),
                                data = deviceMessage.data,
                            )
                        }

                        is DeviceMessage.RPCResponseError -> nats.producer(deviceMessage.id) {
                            send(
                                headers = MapHeaders(
                                    mapOf(
                                        "content-type" to listOf("text/plain"),
                                        "status" to listOf("error"),
                                    )
                                ),
                                data = deviceMessage.message.encodeToByteArray(),
                            )
                        }

                        is DeviceMessage.DeviceInfo -> {
                            functions = deviceMessage.functions
                            events = deviceMessage.events
                        }

                        is DeviceMessage.Pong -> pingLock.synchronize {
                            pingWaiters.remove(deviceMessage.id)
                        }?.resume(Unit)

                        is DeviceMessage.DeviceSpan -> deviceMessage.spans.forEach { span ->
                            zipkinCollector.handleSpan(span)
                        }

                        is DeviceMessage.DeviceLog -> deviceMessage.logs.forEach { log ->
                            if (log.traceId != null) {
                                ZipkinTracing.startTracing(
                                    traceId = log.traceId!!,
                                    spanId = log.spanId,
                                    spanCollector = {
                                        zipkinCollector.handleSpan(it)
                                    }) {
                                    sendLog(log)
                                }
                            } else {
                                sendLog(log)
                            }
                        }
                    }
                }
            }
        } catch (_: WebSocketClosedException) {
            // ignore
        } finally {
            controlListener.asyncCloseAnyway()
            sender.close()
            connection.asyncCloseAnyway()
        }
    }

    private suspend fun sendLog(log: DeviceMessage.Log) {
        logger.log(
            level = logLevel(log.level.toUInt()),
            text = log.message,
            trace = null,
        )
    }

    private val levels = HashMap<UInt, Logger.Level>()
    private val levelLog = SpinLock()

    private fun logLevel(level: UInt) =
        when (level) {
            Logger.DEBUG.priority -> Logger.DEBUG
            Logger.INFO.priority -> Logger.INFO
            Logger.WARNING.priority -> Logger.WARNING
            Logger.SEVERE.priority -> Logger.SEVERE
            else -> levelLog.synchronize {
                levels.getOrPut(level) {
                    object : Logger.Level {
                        override val name: String
                            get() = "L$level"
                        override val priority: UInt
                            get() = level
                    }
                }
            }
        }
}

