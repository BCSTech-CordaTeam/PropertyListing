package com.propertylisting.contracts

import com.propertylisting.contracts.contractTestsMockData.PropertyListingContractTestMockData
import net.corda.core.identity.CordaX500Name
import net.corda.testing.contracts.DummyState
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import org.junit.Test

class PropertyListingContractTest {
    private val ledgerServices = MockServices()
    private val testOwner = TestIdentity(CordaX500Name("TestOwner", "London", "GB"))
    private val testRequester = TestIdentity(CordaX500Name("TestRequester", "New York", "US"))

    @Test
    fun `property list`() {
        val outputState = PropertyListingContractTestMockData().outputState(testOwner,testRequester)
        val invalidOutputState = PropertyListingContractTestMockData().invalidOutputState(testOwner)
        val invalidPropertyAddressOutputState = PropertyListingContractTestMockData().invalidPropertyAddressOutputState(testOwner,testRequester)
        val invalidPropertyAreaOutputState = PropertyListingContractTestMockData().invalidPropertyAreaOutputState(testOwner,testRequester)
        val invalidPropertySellingPriceOutputState = PropertyListingContractTestMockData().invalidPropertySellingPriceOutputState(testOwner,testRequester)

        ledgerServices.ledger {
            transaction {
                command(listOf(testOwner.publicKey, testRequester.publicKey), PropertyListingContract.Commands.STOREPROPERTY())
                output(PropertyListingContract.PROPERTY_CONTRACT_ID, outputState)
                verifies()
            }
            transaction {
                input(PropertyListingContract.PROPERTY_CONTRACT_ID, DummyState())
                command(listOf(testOwner.publicKey, testRequester.publicKey), PropertyListingContract.Commands.STOREPROPERTY())
                output(PropertyListingContract.PROPERTY_CONTRACT_ID, outputState)
                this `fails with` "No inputs should be consumed in transaction"
            }
            transaction {
                command(testOwner.publicKey, PropertyListingContract.Commands.STOREPROPERTY())
                output(PropertyListingContract.PROPERTY_CONTRACT_ID, invalidOutputState)
                this `fails with` "The transaction should have two participants"
            }
            transaction {
                command(testRequester.publicKey, PropertyListingContract.Commands.STOREPROPERTY())
                output(PropertyListingContract.PROPERTY_CONTRACT_ID, outputState)
                this `fails with` "The transaction must be signed by both the participants"
            }
            transaction {
                command(listOf(testOwner.publicKey, testRequester.publicKey), PropertyListingContract.Commands.STOREPROPERTY())
                output(PropertyListingContract.PROPERTY_CONTRACT_ID, invalidPropertyAddressOutputState)
                this `fails with` "Property Address is a mandatory field"
            }
            transaction {
                command(listOf(testOwner.publicKey, testRequester.publicKey), PropertyListingContract.Commands.STOREPROPERTY())
                output(PropertyListingContract.PROPERTY_CONTRACT_ID, invalidPropertyAreaOutputState)
                this `fails with` "Property Area should be greater than zero"
            }
            transaction {
                command(listOf(testOwner.publicKey, testRequester.publicKey), PropertyListingContract.Commands.STOREPROPERTY())
                output(PropertyListingContract.PROPERTY_CONTRACT_ID, invalidPropertySellingPriceOutputState)
                this `fails with` "Property Selling Price should be greater than zero"
            }
        }
    }
}