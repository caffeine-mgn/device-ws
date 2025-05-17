package pw.binom

import pw.binom.device.ws.dto.WsMessage
import pw.binom.io.AsyncCloseable
import pw.binom.io.useAsync
import pw.binom.mq.nats.NatsMqConnection
import pw.binom.mq.nats.NatsProducer

object ControlListener {
    suspend fun create(
        topicName:String,
        nats: NatsMqConnection,
        incomeMessages: suspend (WsMessage) -> Unit,
    ): AsyncCloseable {
        val topic = nats.getOrCreateTopic(topicName)
        val consumer = topic.createConsumer { msg ->
            val id = msg.replyTo ?: return@createConsumer
            incomeMessages(
                WsMessage(
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
