package pw.binom.device.ws.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import pw.binom.tracing.zipkin.Span

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
    @SerialName("response_ok")
    data class RPCResponse(val id: String, val data: ByteArray) : DeviceMessage

    /**
     * Ошибка при выполнении RPC
     */
    @Serializable
    @SerialName("response_error")
    data class RPCResponseError(val id: String, val message: String) : DeviceMessage

    /**
     * Уведомления сервера о событии
     */
    @Serializable
    @SerialName("event")
    data class Event(val data: ByteArray) : DeviceMessage

    @Serializable
    @SerialName("span")
    data class DeviceSpan(val spans: List<Span>) : DeviceMessage

    @Serializable
    data class Log(
        val message: String? = null,
        val level: Int,
        val traceId: String? = null,
        val spanId: String? = null,
        val exception: String? = null,
    )

    @Serializable
    @SerialName("log")
    data class DeviceLog(val spans: List<Log>) : DeviceMessage

    /**
     * Уведомление сервера о предоставляемых функциях и событиях
     */
    @Serializable
    @SerialName("device_info")
    data class DeviceInfo(
        val functions: Map<String, String>,
        val events: Map<String, String>,
    ) : DeviceMessage

    @Serializable
    @SerialName("pong")
    data class Pong(val id: Int) : DeviceMessage
}
