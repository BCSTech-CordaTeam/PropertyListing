package com.propertylisting.flows

import co.paralleluniverse.fibers.Suspendable
import com.propertylisting.flows.utils.CustomVaultService
import com.propertylisting.flows.utils.Property
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.flows.InitiatedBy

@InitiatedBy(RequestPropertyListFlow.Initiator::class)
class SendPropertyListFlow(val session: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call(){
        val customVaultService = serviceHub.cordaService(CustomVaultService.Service::class.java)
        val propertyList : MutableList<Property> = customVaultService.getVacantProperties(serviceHub.myInfo.legalIdentities.first().name.toString())
        session.send(propertyList)
    }
}