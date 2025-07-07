package pw.binom.controllers

import pw.binom.copyTo
import pw.binom.device.ws.dto.WsDevice
import pw.binom.io.http.HttpContentLength
import pw.binom.io.http.httpContentLength
import pw.binom.io.httpServer.HttpHandler
import pw.binom.io.httpServer.HttpServerExchange
import pw.binom.io.httpServer.response
import pw.binom.io.useAsync
import pw.binom.services.StorageService
import pw.binom.strong.inject

class DownloadController : HttpHandler {
    private val storageService: StorageService by inject()
    override suspend fun handle(exchange: HttpServerExchange) {
        if (exchange.requestMethod != "GET") {
            return
        }
        if (!exchange.requestURI.path.isMatch(WsDevice.LOAD_FILE_URI)) {
            return
        }
        val fileName = exchange.path.getVariable("name", WsDevice.LOAD_FILE_URI)!!
        val s3Object = storageService.load(fileName)
        if (s3Object == null) {
            exchange.startResponse(404)
            return
        }
        exchange.response {
            status = 200
            headers.httpContentLength = HttpContentLength.CHUNKED
            startOutput().useAsync { out ->
                s3Object.copyTo(out)
            }
        }
    }
}