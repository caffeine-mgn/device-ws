package pw.binom.device.ws.dto

object WsDevice {
    const val DEVICE_NAME = "X-Client-Name"
    const val DEVICE_ID = "X-Client-Id"
    const val DEVICE_SECRET = "X-Client-Secrets"
    const val DEVICE_MESSAGING_CONTENT_TYPE = "X-Client-Messaging-Content-Type"
    const val BASE_CONTROL_URI = "/api/v1/devices/control"
    const val UPLOAD_FILE_URI = "/api/v1/files"
    const val LOAD_FILE_URI = "/api/v1/files/{name}"
}