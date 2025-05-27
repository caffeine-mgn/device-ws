@file:JvmName("JvmMain")

package pw.binom

import pw.binom.config.DefaultConfig
import pw.binom.strong.StrongApplication
import pw.binom.strong.nats.client.NatsClientConfig
import pw.binom.strong.serialization.SerializationConfig
import pw.binom.strong.web.server.WebConfig
import kotlin.jvm.JvmName

fun main(args: Array<String>) {
    StrongApplication.run(args) {
        +DefaultConfig(properties)
        +NatsClientConfig.apply(properties)
        +WebConfig.apply(properties)
        +SerializationConfig.base()
    }
}