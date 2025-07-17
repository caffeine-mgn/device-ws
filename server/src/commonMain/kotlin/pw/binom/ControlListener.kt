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
            val traceId = msg.headers["trace-id"]?.firstOrNull()
            val spanId = msg.headers["span-id"]?.firstOrNull()
            incomeMessages(
                ServerMessage.RPCRequest(
                    id = id,
                    data = msg.body,
                    traceId = traceId,
                    spanId = spanId,
                )
            )
        }
        return AsyncCloseable {
            consumer.asyncCloseAnyway()
            topic.asyncCloseAnyway()
        }
    }
}
