package pw.binom

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import pw.binom.date.DateTime
import pw.binom.date.iso8601
import pw.binom.logger.Logger
import pw.binom.thread.Thread
import pw.binom.tracing.zipkin.ZipkinTracing

class JsonLogger : Logger.Handler {
    private val json = Json {
        prettyPrint = false
    }

    override suspend fun log(
        logger: Logger,
        level: Logger.Level,
        text: String?,
        trace: String?,
        exception: Throwable?,
    ) {
        val traceId = ZipkinTracing.getTraceId()
        val spanId = ZipkinTracing.getSpanId()
        val log = buildJsonObject {
            put("@timestamp", JsonPrimitive(DateTime.now.iso8601()))
            put("@version", JsonPrimitive("1"))
            put("logger_name", JsonPrimitive(logger.pkg))
            put("thread_name", JsonPrimitive(Thread.currentThread.name))
            if (traceId != null) {
                put("trace_id", JsonPrimitive(traceId))
            }
            if (spanId != null) {
                put("span_id", JsonPrimitive(spanId))
            }
            put("level", JsonPrimitive(level.name))
            put("level_value", JsonPrimitive(level.priority))
            if (exception != null) {
                put("stacktrace", JsonPrimitive(exception.stackTraceToString()))
            }
            if (text != null) {
                put("message", JsonPrimitive(text))
            }
        }
        println(json.encodeToString(JsonElement.serializer(), log))
    }

    override fun logSync(
        logger: Logger,
        level: Logger.Level,
        text: String?,
        trace: String?,
        exception: Throwable?,
    ) {
        val log = buildJsonObject {
            put("@timestamp", JsonPrimitive(DateTime.now.iso8601()))
            put("@version", JsonPrimitive("1"))
            put("logger_name", JsonPrimitive(logger.pkg))
            put("thread_name", JsonPrimitive(Thread.currentThread.name))
            put("level", JsonPrimitive(level.name))
            put("level_value", JsonPrimitive(level.priority))
            if (exception != null) {
                put("stacktrace", JsonPrimitive(exception.stackTraceToString()))
            }
            if (text != null) {
                put("message", JsonPrimitive(text))
            }
        }
        println(json.encodeToString(JsonElement.serializer(), log))
    }
}
