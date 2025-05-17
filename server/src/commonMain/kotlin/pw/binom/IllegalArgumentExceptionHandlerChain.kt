package pw.binom

import kotlinx.serialization.json.Json
import pw.binom.device.ws.dto.ErrorCodes
import pw.binom.device.ws.dto.ErrorMessage
import pw.binom.io.httpServer.HttpHandler
import pw.binom.io.httpServer.HttpHandlerChain
import pw.binom.io.httpServer.HttpServerExchange

class IllegalArgumentExceptionHandlerChain : HttpHandlerChain {
    override suspend fun handle(exchange: HttpServerExchange, next: HttpHandler) {
        try {
            next.handle(exchange)
        } catch (e: IllegalArgumentException) {
            if (exchange.responseStarted) {
                throw e
            }
            val resp = exchange.response()
            val msg = ErrorMessage(
                code = ErrorCodes.ILLEGAL_ARGUMENT,
                message = e.message,
            )
            resp.status = 400
            resp.headers.contentType = "application/json"
            resp.send(Json.encodeToString(ErrorMessage.serializer(), msg))
        }
    }
}