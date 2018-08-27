package com.propertylisting

import com.propertylisting.flows.utils.Property
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.serialization.SerializationWhitelist
import net.corda.webserver.services.WebServerPluginRegistry
import java.util.function.Function

class WebPlugin : WebServerPluginRegistry {
    override val webApis: List<Function<CordaRPCOps, out Any>> = listOf(Function(::PropertyListingApi))
}

class PropertySerializationWhitelist : SerializationWhitelist {
    override val whitelist: List<Class<*>> = listOf(Property::class.java)
}