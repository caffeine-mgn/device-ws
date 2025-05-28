package pw.binom.device.ws.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
@JsonClassDiscriminator("type")
sealed interface DeviceEvent {
    @Serializable
    @SerialName("online")
    data class DeviceOnline(val id: String, val name: String) : DeviceEvent

    @Serializable
    @SerialName("offline")
    data class DeviceOffline(val id: String, val name: String) : DeviceEvent
}