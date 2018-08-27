package com.propertylisting.flows

import com.propertylisting.states.PropertyState
import net.corda.core.contracts.TransactionVerificationException
import net.corda.core.node.services.queryBy
import net.corda.core.utilities.getOrThrow
import net.corda.testing.core.singleIdentity
import net.corda.testing.node.MockNetwork
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertEquals

class PropertyRegistrationFlowTest {
    private val network = MockNetwork(listOf("com.propertylisting"))
    private val testOwner = network.createNode()

    @Before
    fun setup() = network.runNetwork()

    @After
    fun tearDown() = network.stopNodes()

    @Test
    fun `property registration`() {
        val flow = PropertyRegistrationFlow("A-26, New York", 400, 50)
        val propertyRegistration = testOwner.startFlow(flow).getOrThrow()
        testOwner.transaction {
            val outputs = testOwner.services.vaultService.queryBy<PropertyState>().states
            val outputState = outputs.first().state.data
            assertEquals(outputState.propertyAddress, "A-26, New York")
            assertEquals(outputState.propertyArea, 400)
            assertEquals(outputState.propertySellingPrice, 50)
            assertEquals(outputState.owner.owningKey, testOwner.info.singleIdentity().owningKey)
        }
    }

    @Test
    fun `no input state is consumed`() {
        val flow = PropertyRegistrationFlow("A-26, New York", 400, 50)
        val propertyRegistration = testOwner.startFlow(flow).getOrThrow()
        val txn = testOwner.services.validatedTransactions.getTransaction(propertyRegistration!!.id)
        val input = txn!!.tx.inputs
        assert(input.isEmpty())
    }

    @Test
    fun `one output state is created`() {
        val flow = PropertyRegistrationFlow("A-26, New York", 400, 50)
        val propertyRegistration = testOwner.startFlow(flow).getOrThrow()
        val txn = testOwner.services.validatedTransactions.getTransaction(propertyRegistration!!.id)
        val output = txn!!.tx.outputs
        assert(output.size == 1)
    }

    @Test
    fun `the transaction has only one participant`() {
        val flow = PropertyRegistrationFlow("A-26, New York", 400, 50)
        val propertyRegistration = testOwner.startFlow(flow).getOrThrow()
        val txn = testOwner.services.validatedTransactions.getTransaction(propertyRegistration!!.id)
        val output = txn!!.tx.outputs
        assert(output.first().data.participants.size == 1)
    }

    @Test
    fun `the transaction must be signed by the property owner`() {
        val flow = PropertyRegistrationFlow("A-26, New York", 400, 50)
        val propertyRegistration = testOwner.startFlow(flow).getOrThrow()
        propertyRegistration?.verifyRequiredSignatures()
    }

    @Test
    fun `property address is a mandatory field`() {
        val flow = PropertyRegistrationFlow("", 400, 50)
        val propertyRegistration = testOwner.startFlow(flow)
        assertFailsWith<TransactionVerificationException> { propertyRegistration.getOrThrow() }
    }

    @Test
    fun `property area should be greater than zero`() {
        val flow = PropertyRegistrationFlow("A-26, New York", 0, 50)
        val propertyRegistration = testOwner.startFlow(flow)
        assertFailsWith<TransactionVerificationException> { propertyRegistration.getOrThrow() }
    }

    @Test
    fun `property selling price should be greater than zero`() {
        val flow = PropertyRegistrationFlow("A-26, New York", 400, -50)
        val propertyRegistration = testOwner.startFlow(flow)
        assertFailsWith<TransactionVerificationException> { propertyRegistration.getOrThrow() }
    }
}