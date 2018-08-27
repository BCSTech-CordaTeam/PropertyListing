package com.propertylisting

import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.getOrThrow
import net.corda.testing.core.TestIdentity
import net.corda.testing.driver.DriverDSL
import net.corda.testing.driver.DriverParameters
import net.corda.testing.driver.NodeHandle
import net.corda.testing.driver.driver
import org.junit.Test
import java.util.concurrent.Future
import kotlin.test.assertEquals

class DriverBasedTest {
    private val partyA = TestIdentity(CordaX500Name("PartyA", "", "GB"))
    private val partyB = TestIdentity(CordaX500Name("PartyB", "", "US"))

    @Test
    fun `node test`() = withDriver {
        val (partyAHandle, partyBHandle) = startNodes(partyA, partyB)
        assertEquals(partyB.name, partyAHandle.resolveName(partyB.name))
        assertEquals(partyA.name, partyBHandle.resolveName(partyA.name))
    }

    private fun withDriver(test: DriverDSL.() -> Unit) = driver(
        DriverParameters(isDebug = true, startNodesInProcess = true)
    ) { test() }

    private fun NodeHandle.resolveName(name: CordaX500Name) = rpc.wellKnownPartyFromX500Name(name)!!.name

    private fun <T> List<Future<T>>.waitForAll(): List<T> = map { it.getOrThrow() }

    private fun DriverDSL.startNodes(vararg identities: TestIdentity) = identities
        .map { startNode(providedName = it.name) }
        .waitForAll()
}