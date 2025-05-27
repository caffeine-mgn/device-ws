package pw.binom

import pw.binom.device.ws.dto.ServerMessage
import pw.binom.io.AsyncCloseable
import pw.binom.mq.nats.NatsMqConnection

object ControlListener {
    suspend fun create(
        topicName: String,
        nats: NatsMqConnection,
        incomeMessages: suspend (ServerMessage) -> Unit,
    ): AsyncCloseable {
        val topic = nats.getOrCreateTopic(topicName)
        val consumer = topic.createConsumer { msg ->
            val id = msg.replyTo ?: return@createConsumer
            incomeMessages(
                ServerMessage.RPCRequest(
                    id = id,
                    data = msg.body,
                )
            )
        }
        return AsyncCloseable {
            consumer.asyncCloseAnyway()
            topic.asyncCloseAnyway()
        }
    }
}
