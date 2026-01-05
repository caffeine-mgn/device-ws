package pw.binom.device.ws.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import pw.binom.date.DateTime
import pw.binom.device.ws.dto.serialization.DateTimeSecondsSerializer

@Serializable
sealed interface TelemetryEvent {
    @Serializable
    @SerialName("steps")
    data class Steps(
        val steps: Int,
        val calorie: Int,
        val distance: Int,
        @Serializable(DateTimeSecondsSerializer::class)
        val date: DateTime,
    ) : TelemetryEvent

    @Serializable
    @SerialName("ring_changing")
    data class RingChanging(
        val value: Byte,
        @Serializable(DateTimeSecondsSerializer::class)
        val date: DateTime,
    ): TelemetryEvent

    @Serializable
    @SerialName("unknown")
    data class Unknown(
        val cmd: Int,
        val data: ByteArray,
        @Serializable(DateTimeSecondsSerializer::class)
        val date: DateTime,
    ) : TelemetryEvent
}