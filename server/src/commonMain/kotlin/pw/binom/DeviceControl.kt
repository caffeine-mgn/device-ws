package pw.binom

import pw.binom.device.ws.dto.ServerMessage

interface DeviceControl {
    suspend fun income(message: ServerMessage)
}