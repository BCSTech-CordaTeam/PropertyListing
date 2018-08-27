package com.propertylisting.contracts

import com.propertylisting.states.PropertyState
import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction

class PropertyListingContract : Contract {
    companion object {
        @JvmStatic
        val PROPERTY_CONTRACT_ID = "com.propertylisting.contracts.PropertyListingContract"
    }

    interface Commands : CommandData {
        class REGISTER : Commands
        class STOREPROPERTY : Commands
    }

    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands>()
        when (command.value) {
            is Commands.REGISTER -> {
                requireThat {
                    "No inputs should be consumed for propertylisting registration" using (tx.inputs.isEmpty())
                    "Only one output state should be created after propertylisting registration" using (tx.outputs.size == 1)
                    val outputState = tx.outputsOfType<PropertyState>().single()
                    "The transaction should have only one participant" using (outputState.participants.size == 1)
                    "The transaction must be signed by the propertylisting owner" using (outputState.owner.owningKey == command.signers.single())
                    "Property Address is a mandatory field" using (!outputState.propertyAddress.isEmpty())
                    "Property Area should be greater than zero" using (outputState.propertyArea > 0)
                    "Property Selling Price should be greater than zero" using (outputState.propertySellingPrice > 0)
                }
            }
            is Commands.STOREPROPERTY -> {
                requireThat {
                    "No inputs should be consumed in transaction" using (tx.inputs.isEmpty())
                    val outputState = tx.outputsOfType<PropertyState>().first()
                    "The transaction should have two participants" using (outputState.participants.size == 2)
                    "The transaction must be signed by both the participants" using (command.signers.containsAll(outputState.participants.map { it.owningKey }))
                    "Property Address is a mandatory field" using (!outputState.propertyAddress.isEmpty())
                    "Property Area should be greater than zero" using (outputState.propertyArea > 0)
                    "Property Selling Price should be greater than zero" using (outputState.propertySellingPrice > 0)
                }
            }
        }
    }
}