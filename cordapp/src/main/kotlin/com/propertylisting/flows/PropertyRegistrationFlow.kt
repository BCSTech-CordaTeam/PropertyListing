package com.propertylisting.flows

import co.paralleluniverse.fibers.Suspendable
import com.propertylisting.contracts.PropertyListingContract
import com.propertylisting.contracts.PropertyListingContract.Companion.PROPERTY_CONTRACT_ID
import com.propertylisting.flows.utils.CustomVaultService
import com.propertylisting.states.PropertyState
import net.corda.core.contracts.Command
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step
import net.corda.core.flows.FinalityFlow

@StartableByRPC
class PropertyRegistrationFlow(
        val propertyAddress : String,
        val propertyArea : Long,
        val propertySellingPrice : Long) : FlowLogic<SignedTransaction?>() {
    companion object {
        object GENERATING_REGISTRATION_TRANSACTION : Step("Generating transaction for Property Registration.")
        object VERIFYING_REGISTRATION_TRANSACTION : Step("Verifying contract constraints.")
        object SIGNING_REGISTRATION_TRANSACTION : Step("Signing transaction with propertylisting owner's private key.")
        object FINALISING_REGISTRATION_TRANSACTION : Step("Obtaining notary signature and recording transaction.") {
            override fun childProgressTracker() = FinalityFlow.tracker() }
        fun tracker() = ProgressTracker(
                GENERATING_REGISTRATION_TRANSACTION,
                VERIFYING_REGISTRATION_TRANSACTION,
                SIGNING_REGISTRATION_TRANSACTION,
                FINALISING_REGISTRATION_TRANSACTION
        )
    }
    override val progressTracker = tracker()

    @Suspendable
    override fun call(): SignedTransaction?{
        val customVaultService = serviceHub.cordaService(CustomVaultService.Service::class.java)
        val propertyExists : Boolean = customVaultService.checkPropertyExistence(serviceHub.myInfo.legalIdentities.first().name.toString(),propertyAddress)

        if(!propertyExists) {
            val notary: Party = serviceHub.networkMapCache.notaryIdentities.first()

            progressTracker.currentStep = GENERATING_REGISTRATION_TRANSACTION
            val propertyState = PropertyState(owner = serviceHub.myInfo.legalIdentities.first(), propertyAddress = propertyAddress,
                                                            propertyArea = propertyArea, propertySellingPrice = propertySellingPrice)
            val txCommand = Command(PropertyListingContract.Commands.REGISTER(), propertyState.participants.map { it.owningKey })
            val txBuilder = TransactionBuilder(notary)
                    .addOutputState(propertyState, PROPERTY_CONTRACT_ID)
                    .addCommand(txCommand)

            progressTracker.currentStep = VERIFYING_REGISTRATION_TRANSACTION
            txBuilder.verify(serviceHub)

            progressTracker.currentStep = SIGNING_REGISTRATION_TRANSACTION
            val signedTx = serviceHub.signInitialTransaction(txBuilder)

            progressTracker.currentStep = FINALISING_REGISTRATION_TRANSACTION
            return subFlow(FinalityFlow(signedTx))
        }
        else
            return null
    }
}