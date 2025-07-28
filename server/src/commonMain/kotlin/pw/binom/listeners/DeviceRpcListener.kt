package pw.binom.listeners

import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withTimeout
import pw.binom.device.ws.dto.ServerMessage
import pw.binom.io.useAsync
import pw.binom.logger.Logger
import pw.binom.logger.info
import pw.binom.logger.warn
import pw.binom.mq.MapHeaders
import pw.binom.mq.nats.NatsMqConnection
import pw.binom.mq.nats.client.NatsMessage
import pw.binom.producer
import pw.binom.properties.ApplicationProperties
import pw.binom.services.DevicesControlService
import pw.binom.strong.inject
import pw.binom.strong.nats.client.AbstractNatsConsumer
import pw.binom.strong.nats.client.NatsConsumerProperties
import pw.binom.strong.properties.injectProperty
import kotlin.time.Duration.Companion.seconds

class DeviceRpcListener : AbstractNatsConsumer() {
    private val connection1: NatsMqConnection by inject()
    private val applicationProperties: ApplicationProperties by injectProperty()
    private val devicesControlService: DevicesControlService by inject()
    private val logger by Logger.ofThisOrGlobal
    override val consumerConfig: NatsConsumerProperties
        get() = NatsConsumerProperties(
            topic = "${applicationProperties.topicPrefix}.*.rpc"
        )

    override suspend fun income(message: NatsMessage) {
        val deviceId = message.subject.removePrefix("${applicationProperties.topicPrefix}.").removeSuffix(".rpc")
        logger.info("Income message from device $deviceId")
        val id = message.replyTo
        if (id == null) {
            logger.warn("Income message, but replyTo missing")
            return
        }

        val traceId = message.headers["trace-id"]?.firstOrNull()
        val spanId = message.headers["span-id"]?.firstOrNull()
        logger.info("Searching device \"$deviceId\"...")
        val device = devicesControlService.findById(deviceId)
        logger.info("Device \"${deviceId}\" found!!!")
        if (device == null) {
            connection1.producer(id) {
                send(
                    MapHeaders(
                        mapOf(
                            "content-type" to listOf("text/plain"),
                            "status" to listOf("error"),
                        )
                    ),
                    "Device not connected".encodeToByteArray()
                )
            }
        } else {
            val msg = ServerMessage.RPCRequest(
                id = id,
                data = message.body,
                traceId = traceId,
                spanId = spanId,
            )
            logger.info("Income message to $deviceId: $msg")
            try {
                supervisorScope {
                    device.income(msg)
                }
                logger.info("Message successful sent!")
            } catch (e: Throwable) {
                logger.warn("Can't send to $deviceId - timeout!")
            }
        }
    }
}