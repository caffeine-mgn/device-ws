package pw.binom.device.ws.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

/**
 * Устройство шлёт серверу
 */
@Serializable
@JsonClassDiscriminator("type")
sealed interface DeviceMessage {
    /**
     * Ответ на RPC
     */
    @Serializable
    data class RPCResponse(val id: String, val data: ByteArray) : DeviceMessage

    /**
     * Ошибка при выполнении RPC
     */
    @Serializable
    data class RPCResponseError(val id: String, val message: String) : DeviceMessage

    /**
     * Уведомления сервера о событии
     */
    @Serializable
    data class Event(val data: ByteArray) : DeviceMessage

    /**
     * Уведомление сервера о предоставляемых функциях и событиях
     */
    @Serializable
    data class DeviceInfo(
        val functions: Map<String, String>,
        val events: Map<String, String>,
    ) : DeviceMessage

    @Serializable
    data class Pong(val id: Int) : DeviceMessage
}
