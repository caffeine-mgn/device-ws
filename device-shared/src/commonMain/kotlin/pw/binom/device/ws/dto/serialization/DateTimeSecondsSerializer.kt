package pw.binom.device.ws.dto.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import pw.binom.date.DateTime

object DateTimeSecondsSerializer : KSerializer<DateTime> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("DateTime", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: DateTime) {
        encoder.encodeLong(value.milliseconds / 1000)
    }

    override fun deserialize(decoder: Decoder): DateTime =
        DateTime(decoder.decodeLong() * 1000)
}