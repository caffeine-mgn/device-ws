package pw.binom.services

import pw.binom.DeviceControlWs
import pw.binom.device.ws.dto.DeviceEvent
import pw.binom.mq.MapHeaders
import pw.binom.properties.ApplicationProperties
import pw.binom.strong.inject
import pw.binom.strong.nats.client.AbstractNatsProducer
import pw.binom.strong.properties.injectProperty
import pw.binom.strong.serialization.SerializationService

class DeviceStatusEmitterService : AbstractNatsProducer() {
    private val serializationService: SerializationService by inject()
    private val applicationProperties: ApplicationProperties by injectProperty()
    private val defaultMimeCode = "application/json"

    suspend fun deviceOnline(control: DeviceControlWs) {
        val data = serializationService.encode(
            mimeType = defaultMimeCode,
            DeviceEvent.serializer(),
            DeviceEvent.DeviceOnline(
                id = control.id,
                name = control.name,
            )
        )
        producer.send(headers = MapHeaders(mapOf("content-type" to listOf(defaultMimeCode))), data = data)
    }

    suspend fun deviceOffline(control: DeviceControlWs) {
        val data = serializationService.encode(
            mimeType = defaultMimeCode,
            DeviceEvent.serializer(),
            DeviceEvent.DeviceOffline(
                id = control.id,
                name = control.name,
            )
        )

        producer.send(headers = MapHeaders(mapOf("content-type" to listOf(defaultMimeCode))), data = data)
    }

    override val topicName: String
        get() = "${applicationProperties.topicPrefix}.events"
}