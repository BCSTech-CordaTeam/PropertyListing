package com.propertylisting.contracts

import com.propertylisting.contracts.PropertyListingContract.Companion.PROPERTY_CONTRACT_ID
import com.propertylisting.contracts.contractTestsMockData.PropertyRegistrationContractTestMockData
import net.corda.core.identity.CordaX500Name
import net.corda.testing.contracts.DummyState
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import org.junit.Test

class PropertyRegistrationContractTest {
    private val ledgerServices = MockServices()
    private val testOwner = TestIdentity(CordaX500Name("TestOwner", "London", "GB"))
    private val testRequester = TestIdentity(CordaX500Name("TestRequester", "New York", "US"))

    @Test
    fun `property registration`() {
        val outputState = PropertyRegistrationContractTestMockData().outputState(testOwner)
        val invalidOutputState = PropertyRegistrationContractTestMockData().invalidOutputState(testOwner,testRequester)
        val invalidPropertyAddressOutputState = PropertyRegistrationContractTestMockData().invalidPropertyAddressOutputState(testOwner)
        val invalidPropertyAreaOutputState = PropertyRegistrationContractTestMockData().invalidPropertyAreaOutputState(testOwner)
        val invalidPropertySellingPriceOutputState = PropertyRegistrationContractTestMockData().invalidPropertySellingPriceOutputState(testOwner)

        ledgerServices.ledger {
            transaction {
                command(testOwner.publicKey, PropertyListingContract.Commands.REGISTER())
                output(PROPERTY_CONTRACT_ID, outputState)
                verifies()
            }
            transaction {
                input(PROPERTY_CONTRACT_ID, DummyState())
                command(testOwner.publicKey, PropertyListingContract.Commands.REGISTER())
                output(PROPERTY_CONTRACT_ID, outputState)
                this `fails with` "No inputs should be consumed for propertylisting registration"
            }
            transaction {
                command(testOwner.publicKey, PropertyListingContract.Commands.REGISTER())
                output(PROPERTY_CONTRACT_ID, outputState)
                output(PROPERTY_CONTRACT_ID, outputState)
                this `fails with` "Only one output state should be created after propertylisting registration"
            }
            transaction {
                command(testOwner.publicKey, PropertyListingContract.Commands.REGISTER())
                output(PROPERTY_CONTRACT_ID, invalidOutputState)
                this `fails with` "The transaction should have only one participant"
            }
            transaction {
                command(testRequester.publicKey, PropertyListingContract.Commands.REGISTER())
                output(PROPERTY_CONTRACT_ID, outputState)
                this `fails with` "The transaction must be signed by the propertylisting owner"
            }
            transaction {
                command(testOwner.publicKey, PropertyListingContract.Commands.REGISTER())
                output(PROPERTY_CONTRACT_ID, invalidPropertyAddressOutputState)
                this `fails with` "Property Address is a mandatory field"
            }
            transaction {
                command(testOwner.publicKey, PropertyListingContract.Commands.REGISTER())
                output(PROPERTY_CONTRACT_ID, invalidPropertyAreaOutputState)
                this `fails with` "Property Area should be greater than zero"
            }
            transaction {
                command(testOwner.publicKey, PropertyListingContract.Commands.REGISTER())
                output(PROPERTY_CONTRACT_ID, invalidPropertySellingPriceOutputState)
                this `fails with` "Property Selling Price should be greater than zero"
            }
        }
    }
}