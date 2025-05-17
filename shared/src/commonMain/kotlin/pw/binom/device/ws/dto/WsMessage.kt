package pw.binom.device.ws.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoBuf
import pw.binom.io.AsyncInput
import pw.binom.io.AsyncOutput
import pw.binom.io.readBytes

@Serializable
data class WsMessage(
    @SerialName("i")
    val id: String,
    val data: ByteArray,
) {
    companion object {
        suspend fun read(input: AsyncInput): WsMessage {
            val size = input.readInt()
            return ProtoBuf.decodeFromByteArray(serializer(), input.readBytes(size))
        }
    }

    suspend fun write(output: AsyncOutput) {
        val bytes = ProtoBuf.encodeToByteArray(serializer(), this)
        output.writeInt(bytes.size)
        output.write(bytes)
    }
}