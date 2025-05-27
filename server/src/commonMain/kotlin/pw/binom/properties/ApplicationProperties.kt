package pw.binom.properties

import kotlinx.serialization.Serializable
import pw.binom.properties.serialization.annotations.PropertiesPrefix
import kotlin.time.Duration.Companion.seconds

@PropertiesPrefix("app")
@Serializable
data class ApplicationProperties(
    val deviceSecret: String? = null,
    val topicPrefix: String = "device",
    val pingInterval: Long = 30.seconds.inWholeMilliseconds,
    val pingTimeout: Long = 10.seconds.inWholeMilliseconds,
)