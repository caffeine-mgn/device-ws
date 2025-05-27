package pw.binom.device.ws.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@Serializable
@JsonClassDiscriminator("type")
sealed interface DeviceEvent {
    @Serializable
    data class DeviceOnline(val id: String, val name: String) : DeviceEvent

    @Serializable
    data class DeviceOffline(val id: String, val name: String) : DeviceEvent
}