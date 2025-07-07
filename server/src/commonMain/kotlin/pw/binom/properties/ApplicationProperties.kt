package pw.binom.properties

import kotlinx.serialization.Serializable
import pw.binom.properties.serialization.annotations.PropertiesPrefix
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@PropertiesPrefix("app")
@Serializable
data class ApplicationProperties(
    val deviceSecret: String? = null,
    val topicPrefix: String = "device",
    val pingInterval: Duration = 30.seconds,
    val pingTimeout: Duration = 10.seconds,
    val storage: Storage,
) {
    @Serializable
    data class Storage(
        val accessKey: String,
        val secretKey: String,
        val url: String,
        val bucketName: String,
        val regin: String,
    )
}