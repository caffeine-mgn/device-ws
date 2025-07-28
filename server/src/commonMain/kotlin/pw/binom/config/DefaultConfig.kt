package pw.binom.config

import pw.binom.IllegalArgumentExceptionHandlerChain
import pw.binom.controllers.DeviceControlWsController
import pw.binom.controllers.DownloadController
import pw.binom.controllers.UploadController
import pw.binom.http.client.HttpClientRunnable
import pw.binom.http.client.factory.Https11ConnectionFactory
import pw.binom.http.client.factory.NativeNetChannelFactory
import pw.binom.listeners.DeviceRpcListener
import pw.binom.network.NetworkManager
import pw.binom.services.DeviceStatusEmitterService
import pw.binom.services.DevicesControlService
import pw.binom.services.StorageService
import pw.binom.services.StrongS3Client
import pw.binom.strong.Strong
import pw.binom.strong.bean
import pw.binom.strong.beanAsyncCloseable
import pw.binom.strong.properties.StrongProperties
import pw.binom.strong.serialization.providers.JsonProvider
import pw.binom.strong.serialization.providers.ProtoBufProvider
import pw.binom.traycing.strong.config.ZipkinConfig

fun DefaultConfig(config: StrongProperties,networkManager: NetworkManager) = Strong.config {
    it.beanAsyncCloseable {
        HttpClientRunnable(
            idleCoroutineContext = networkManager,
            factory = Https11ConnectionFactory(),
            source = NativeNetChannelFactory(networkManager)
        )
    }
    ZipkinConfig().apply(it)
    it.bean { DeviceControlWsController() }
    it.bean { DevicesControlService() }
    it.bean { DeviceRpcListener() }
    it.bean { IllegalArgumentExceptionHandlerChain() }
    it.bean { JsonProvider() }
    it.bean { ProtoBufProvider() }
    it.bean { DeviceStatusEmitterService() }
    it.bean { StorageService() }
    it.bean { StrongS3Client() }
    it.bean { DownloadController() }
    it.bean { UploadController() }
}