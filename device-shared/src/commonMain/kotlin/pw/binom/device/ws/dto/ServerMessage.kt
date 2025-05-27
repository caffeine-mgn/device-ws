package pw.binom.device.ws.dto

import kotlinx.serialization.Serializable

/**
 * Сервер шлёт устройству
 */
@Serializable
sealed interface ServerMessage {
    /**
     * Посылает запрос на выполнение функции
     */
    @Serializable
    data class RPCRequest(val id: String, val data: ByteArray) : ServerMessage

    /**
     * Шлёт запрос на ping
     */
    data class Ping(val id: Int) : ServerMessage
}