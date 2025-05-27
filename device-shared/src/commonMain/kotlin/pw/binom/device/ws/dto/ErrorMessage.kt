package pw.binom.device.ws.dto

import kotlinx.serialization.Serializable

@Serializable
data class ErrorMessage(
    val code: ErrorCodes,
    val message: String? = null,
)