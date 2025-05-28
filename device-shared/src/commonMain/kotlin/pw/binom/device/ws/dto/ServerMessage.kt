package pw.binom.device.ws.dto

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
    data class RPCRequest(val id: String, val data: ByteArray) : ServerMessage

    /**
     * Шлёт запрос на ping
     */
    @Serializable
    data class Ping(val id: Int) : ServerMessage
}