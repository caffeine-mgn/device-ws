package pw.binom.device.ws.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ErrorCodes {
    @SerialName("illegal_argument")
    ILLEGAL_ARGUMENT,
}