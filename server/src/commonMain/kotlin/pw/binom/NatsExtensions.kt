package pw.binom

import pw.binom.mq.nats.NatsMqConnection
import pw.binom.mq.nats.NatsProducer
import pw.binom.io.useAsync

suspend fun <T> NatsMqConnection.producer(topic: String, func: suspend NatsProducer.() -> T): T =
    getOrCreateTopic(topic).useAsync { topic ->
        topic.createProducer().useAsync { producer ->
            func(producer)
        }
    }