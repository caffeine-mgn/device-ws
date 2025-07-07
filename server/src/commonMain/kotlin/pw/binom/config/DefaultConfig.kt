package pw.binom.config

import pw.binom.IllegalArgumentExceptionHandlerChain
import pw.binom.controllers.DeviceControlWsController
import pw.binom.controllers.DownloadController
import pw.binom.controllers.UploadController
import pw.binom.services.DeviceStatusEmitterService
import pw.binom.services.DevicesControlService
import pw.binom.services.StorageService
import pw.binom.services.StrongS3Client
import pw.binom.strong.Strong
import pw.binom.strong.bean
import pw.binom.strong.properties.StrongProperties
import pw.binom.strong.serialization.providers.JsonProvider
import pw.binom.strong.serialization.providers.ProtoBufProvider

fun DefaultConfig(config: StrongProperties) = Strong.config {
    it.bean { DeviceControlWsController() }
    it.bean { DevicesControlService() }
    it.bean { IllegalArgumentExceptionHandlerChain() }
    it.bean { JsonProvider() }
    it.bean { ProtoBufProvider() }
    it.bean { DeviceStatusEmitterService() }
    it.bean { StorageService() }
    it.bean { StrongS3Client() }
    it.bean { DownloadController() }
    it.bean { UploadController() }
}