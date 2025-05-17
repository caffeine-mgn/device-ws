package pw.binom

import kotlinx.coroutines.isActive
import pw.binom.device.ws.dto.WsMessage
import pw.binom.io.http.websocket.MessageType
import pw.binom.io.http.websocket.WebSocketClosedException
import pw.binom.io.http.websocket.WebSocketConnection
import pw.binom.io.useAsync
import pw.binom.logger.Logger
import pw.binom.logger.info
import pw.binom.mq.Headers
import pw.binom.mq.MapHeaders
import pw.binom.mq.nats.NatsMqConnection
import pw.binom.mq.nats.client.ParsedHeadersMap
import pw.binom.network.NetworkManager
import kotlin.coroutines.coroutineContext

class DeviceControl(
    val id: String,
    val name: String,
    private val connection: WebSocketConnection,
    private val networkManager: NetworkManager,
    private val nats: NatsMqConnection,
    private val messageContentType: String,
) {
    private val logger = Logger.getLogger("Device $id")

    private val sender = ProcessingChannel<WsMessage> { input ->
        connection.write(MessageType.BINARY).useAsync { output ->
            logger.info("Send to device ${input.id} ${input.data.size} bytes")
            input.write(output)
        }
    }

    suspend fun income(message: WsMessage) {
        logger.info("Income for send to device ${message.id} ${message.data.size} bytes")
        sender.push(message)
    }

    suspend fun processing() {
        sender.start(networkManager)
        val controlListener = ControlListener.create(
            topicName = "devices.$id.control",
            nats = nats,
            incomeMessages = this::income,
        )
        try {
            while (coroutineContext.isActive) {
                connection.read().useAsync { input ->
                    val clientRequest = WsMessage.read(input)
                    logger.info("Income from device ${clientRequest.id} ${clientRequest.data.size} bytes")
                    nats.producer(clientRequest.id) {
                        val headers = MapHeaders(mapOf("content-type" to listOf(messageContentType)))
                        send(headers, clientRequest.data)
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
}

