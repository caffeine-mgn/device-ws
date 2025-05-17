package pw.binom.properties

import kotlinx.serialization.Serializable
import pw.binom.properties.serialization.annotations.PropertiesPrefix

@PropertiesPrefix("app")
@Serializable
data class ApplicationProperties(
    val deviceSecret: String? = null,
)