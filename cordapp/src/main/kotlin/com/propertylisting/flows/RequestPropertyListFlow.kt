package com.propertylisting.flows

import co.paralleluniverse.fibers.Suspendable
import com.propertylisting.flows.utils.CustomVaultService
import com.propertylisting.flows.utils.Property
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.unwrap

object RequestPropertyListFlow {
    @InitiatingFlow
    @StartableByRPC
    class Initiator(val owner: Party) : FlowLogic<SignedTransaction?>() {
        companion object {
            object GENERATING_PROPERTYLIST_REQUEST_SESSION : ProgressTracker.Step("Generating session to request an owner for propertylisting list.")
            object REQUESTING_PROPERTYLIST : ProgressTracker.Step("Requesting owner for the propertylisting list.")
            fun tracker() = ProgressTracker(
                    GENERATING_PROPERTYLIST_REQUEST_SESSION,
                    REQUESTING_PROPERTYLIST)
        }
        override val progressTracker = tracker()

        @Suspendable
        override fun call() : SignedTransaction? {
            progressTracker.currentStep = GENERATING_PROPERTYLIST_REQUEST_SESSION
            val session = initiateFlow(owner)
            progressTracker.currentStep = REQUESTING_PROPERTYLIST
            val list = session.receive<MutableList<Property>>().unwrap { it }
            if (list.isNotEmpty()) {
                val customVaultService = serviceHub.cordaService(CustomVaultService.Service::class.java)
                val propertyList: MutableList<Property> = customVaultService.removeExistingProperties(owner.name.toString(),list)
                if(propertyList.isNotEmpty())
                    return subFlow(StorePropertyListFlow.Initiator(owner,propertyList))
            }
            return null
        }
    }
}