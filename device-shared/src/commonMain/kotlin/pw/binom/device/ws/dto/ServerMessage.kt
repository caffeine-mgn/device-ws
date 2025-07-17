package pw.binom.device.ws.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

/**
 * Сервер шлёт устройству
 */
@Serializable
@JsonClassDiscriminator("type")
sealed interface ServerMessage {
    /**
     * Посылает запрос на выполнение функции
     */
    @Serializable
    @SerialName("request")
    data class RPCRequest(
        val id: String,
        val data: ByteArray,
        val traceId: String?,
        val spanId: String?,
    ) : ServerMessage

    /**
     * Шлёт запрос на ping
     */
    @Serializable
    @SerialName("ping")
    data class Ping(val id: Int) : ServerMessage
}