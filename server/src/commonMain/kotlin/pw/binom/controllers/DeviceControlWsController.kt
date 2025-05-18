package pw.binom.controllers

import pw.binom.device.ws.dto.WsDevice
import pw.binom.illegalArgument
import pw.binom.io.httpServer.HttpHandler
import pw.binom.io.httpServer.HttpServerExchange
import pw.binom.io.httpServer.acceptWebsocket
import pw.binom.logger.Logger
import pw.binom.logger.info
import pw.binom.properties.ApplicationProperties
import pw.binom.services.DevicesControlService
import pw.binom.strong.inject
import pw.binom.strong.properties.injectProperty

class DeviceControlWsController : HttpHandler {
    private val devicesControlService: DevicesControlService by inject()
    private val logger by Logger.ofThisOrGlobal
    private val applicationProperties by injectProperty<ApplicationProperties>()
    private fun missingHeader(headerName: String): Nothing = illegalArgument { "Missing header \"$headerName\"" }
    override suspend fun handle(exchange: HttpServerExchange) {
        logger.info("Income request ${exchange.requestMethod} ${exchange.requestURI}")
        if (exchange.requestMethod != "GET") {
            return
        }
        if (!exchange.requestURI.path.isMatch(WsDevice.BASE_CONTROL_URI)) {
            return
        }
        val deviceId =
            exchange.requestHeaders.getSingleOrNull(WsDevice.DEVICE_ID) ?: missingHeader(WsDevice.DEVICE_ID)
        val deviceName =
            exchange.requestHeaders.getSingleOrNull(WsDevice.DEVICE_NAME) ?: missingHeader(WsDevice.DEVICE_NAME)
        if (applicationProperties.deviceSecret != null) {
            val deviceSecret =
                exchange.requestHeaders.getSingleOrNull(WsDevice.DEVICE_SECRET) ?: missingHeader(WsDevice.DEVICE_SECRET)

            if (deviceSecret != applicationProperties.deviceSecret) {
                throw IllegalArgumentException("Invalid secret device key")
            }
        }

        val deviceMessageContentType = exchange.requestHeaders.getSingleOrNull(WsDevice.DEVICE_MESSAGING_CONTENT_TYPE)
            ?: missingHeader(WsDevice.DEVICE_MESSAGING_CONTENT_TYPE)
        logger.info("Income connection:\n  deviceId: $deviceId\n  deviceName: ${deviceName}")
        val client = exchange.acceptWebsocket()
        try {
            devicesControlService.controlProcessing(
                deviceId = deviceId,
                deviceName = deviceName,
                connection = client,
                messageContentType = deviceMessageContentType,
            )
        } finally {
            client.asyncCloseAnyway()
        }
    }
}