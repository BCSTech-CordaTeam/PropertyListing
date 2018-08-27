package com.propertylisting.flows

import co.paralleluniverse.fibers.Suspendable
import com.propertylisting.contracts.PropertyListingContract
import com.propertylisting.flows.utils.Property
import com.propertylisting.states.PropertyState
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step

object StorePropertyListFlow {
    @InitiatingFlow
    @StartableByRPC
    class Initiator(val owner: Party, val propertyList: MutableList<Property>) : FlowLogic<SignedTransaction>() {
        companion object {
            object GENERATING_STORE_PROPERTY_LIST_TRANSACTION : Step("Generating transaction for storing property list.")
            object VERIFYING_STORE_PROPERTY_LIST_TRANSACTION : Step("Verifying contract constraints.")
            object SIGNING_STORE_PROPERTY_LIST_TRANSACTION : Step("Signing transaction with requester's private key.")
            object GATHERING_OWNER_SIGS : Step("Gathering the owner's signature."){
                override fun childProgressTracker() = CollectSignaturesFlow.tracker() }
            object FINALISING_STORE_PROPERTY_LIST_TRANSACTION : Step("Obtaining notary signature and recording transaction.") {
                override fun childProgressTracker() = FinalityFlow.tracker() }
            fun tracker() = ProgressTracker(
                    GENERATING_STORE_PROPERTY_LIST_TRANSACTION,
                    VERIFYING_STORE_PROPERTY_LIST_TRANSACTION,
                    SIGNING_STORE_PROPERTY_LIST_TRANSACTION,
                    GATHERING_OWNER_SIGS,
                    FINALISING_STORE_PROPERTY_LIST_TRANSACTION)
        }
        override val progressTracker = tracker()

        @Suspendable
        override fun call(): SignedTransaction {
            val notary: Party = serviceHub.networkMapCache.notaryIdentities.first()

            progressTracker.currentStep = GENERATING_STORE_PROPERTY_LIST_TRANSACTION
            val txCommand = Command(PropertyListingContract.Commands.STOREPROPERTY(), listOf(owner.owningKey, serviceHub.myInfo.legalIdentities.first().owningKey))
            val txBuilder = TransactionBuilder(notary)
                            .addCommand(txCommand)
            for(property in propertyList)
            {
                val propertyState = PropertyState(owner = owner, requester = serviceHub.myInfo.legalIdentities.first(), propertyAddress = property.propertyAddress,
                                                                        propertyArea = property.propertyArea, propertySellingPrice = property.propertySellingPrice)
                txBuilder.addOutputState(propertyState, PropertyListingContract.PROPERTY_CONTRACT_ID)
            }

            progressTracker.currentStep = VERIFYING_STORE_PROPERTY_LIST_TRANSACTION
            txBuilder.verify(serviceHub)

            progressTracker.currentStep = SIGNING_STORE_PROPERTY_LIST_TRANSACTION
            val partSignedTx = serviceHub.signInitialTransaction(txBuilder)

            progressTracker.currentStep = GATHERING_OWNER_SIGS
            val otherPartyFlow = initiateFlow(owner)
            val fullySignedTx = subFlow(CollectSignaturesFlow(partSignedTx, setOf(otherPartyFlow), GATHERING_OWNER_SIGS.childProgressTracker()))

            progressTracker.currentStep = FINALISING_STORE_PROPERTY_LIST_TRANSACTION
            return subFlow(FinalityFlow(fullySignedTx))
        }
    }

    @InitiatedBy(StorePropertyListFlow.Initiator::class)
    class Responder(val session: FlowSession) : FlowLogic<SignedTransaction>() {
        @Suspendable
        override fun call(): SignedTransaction {
            val signTransactionFlow = object : SignTransactionFlow(session) {
                override fun checkTransaction(stx: SignedTransaction) = requireThat {
                    "No inputs should be consumed in transaction" using (stx.tx.inputs.isEmpty())
                }
            }
            return subFlow(signTransactionFlow)
        }
    }
}