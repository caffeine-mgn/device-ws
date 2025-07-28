package pw.binom.services

import kotlinx.coroutines.channels.Channel
import pw.binom.DeviceControlWs
import pw.binom.concurrency.ReentrantLock
import pw.binom.concurrency.synchronize
import pw.binom.device.ws.dto.ServerMessage
import pw.binom.io.http.websocket.WebSocketConnection
import pw.binom.logger.Logger
import pw.binom.logger.info
import pw.binom.mq.nats.NatsMqConnection
import pw.binom.network.NetworkManager
import pw.binom.properties.ApplicationProperties
import pw.binom.strong.inject
import pw.binom.strong.properties.injectProperty
import pw.binom.traycing.strong.ZipkinCollector

class DevicesControlService {
    private val connections = HashMap<WebSocketConnection, DeviceControlWs>()
    private val connectionsById = HashMap<String, DeviceControlWs>()
    private val lock = ReentrantLock()
    private val logger by Logger.ofThisOrGlobal
    private val networkManager: NetworkManager by inject()
    private val deviceStatusEmitterService: DeviceStatusEmitterService by inject()
    private val nats: NatsMqConnection by inject()
    private val applicationProperties: ApplicationProperties by injectProperty()
    private val zipkinCollector: ZipkinCollector by inject()

    val devices: List<DeviceControlWs>
        get() = lock.synchronize {
            ArrayList(connections.values)
        }

    fun findById(id: String) = lock.synchronize {
        connectionsById[id]
    }

    suspend fun controlProcessing(
        deviceId: String,
        deviceName: String,
        connection: WebSocketConnection,
        messageContentType: String,
    ) {
        logger.info("Connected $deviceId:$deviceName")
        val deviceControl = DeviceControlWs(
            id = deviceId,
            name = deviceName,
            connection = connection,
            networkManager = networkManager,
            nats = nats,
            messageContentType = messageContentType,
            topicPrefix = applicationProperties.topicPrefix,
            pingInterval = applicationProperties.pingInterval,
            pingTimeout = applicationProperties.pingTimeout,
            zipkinCollector = zipkinCollector,
        )
        lock.synchronize {
            connections[connection] = deviceControl
            connectionsById[deviceId] = deviceControl
        }
        try {
            deviceStatusEmitterService.deviceOnline(deviceControl)
            deviceControl.processing()
        } finally {
            deviceStatusEmitterService.deviceOffline(deviceControl)
            logger.info("Disconnected $deviceId:$deviceName")
            lock.synchronize {
                connections.remove(connection)
                connectionsById.remove(deviceId)
            }
        }
    }
}