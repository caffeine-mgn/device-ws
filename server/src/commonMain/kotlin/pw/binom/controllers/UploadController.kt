package pw.binom.controllers

import pw.binom.device.ws.dto.WsDevice
import pw.binom.io.httpServer.HttpHandler
import pw.binom.io.httpServer.HttpServerExchange
import pw.binom.io.httpServer.response
import pw.binom.services.StorageService
import pw.binom.strong.inject

class UploadController : HttpHandler {
    private val storageService: StorageService by inject()
    override suspend fun handle(exchange: HttpServerExchange) {
        if (exchange.requestMethod != "POST") {
            return
        }
        if (!exchange.requestURI.path.isMatch(WsDevice.UPLOAD_FILE_URI)) {
            return
        }
        val resultKey = storageService.storage(exchange.input)
        exchange.response {
            status = 200
            send(resultKey)
        }
    }
}