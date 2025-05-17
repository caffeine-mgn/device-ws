package pw.binom.config

import pw.binom.IllegalArgumentExceptionHandlerChain
import pw.binom.controllers.DeviceControlWsController
import pw.binom.services.DevicesControlService
import pw.binom.strong.Strong
import pw.binom.strong.bean
import pw.binom.strong.properties.StrongProperties

fun DefaultConfig(config: StrongProperties) = Strong.config {
    it.bean { DeviceControlWsController() }
    it.bean { DevicesControlService() }
    it.bean { IllegalArgumentExceptionHandlerChain() }
}