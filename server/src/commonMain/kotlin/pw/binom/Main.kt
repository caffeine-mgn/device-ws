@file:JvmName("JvmMain")

package pw.binom

import pw.binom.config.DefaultConfig
import pw.binom.logger.Logger
import pw.binom.strong.StrongApplication
import pw.binom.strong.nats.client.NatsClientConfig
import pw.binom.strong.serialization.SerializationConfig
import pw.binom.strong.web.server.WebConfig
import kotlin.jvm.JvmName

fun main(args: Array<String>) {
    if (Environment.getEnv("KUBERNETES_SERVICE_HOST") != null) {
        Logger.global.handler = JsonLogger()
    }
    StrongApplication.run(args) {
        +DefaultConfig(properties, networkManager)
        +NatsClientConfig.apply(properties)
        +WebConfig.apply(properties)
        +SerializationConfig.base()
    }
}