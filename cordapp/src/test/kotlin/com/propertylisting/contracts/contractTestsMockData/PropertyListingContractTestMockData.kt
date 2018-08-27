package com.propertylisting.contracts.contractTestsMockData

import com.propertylisting.states.PropertyState
import net.corda.testing.core.TestIdentity

class PropertyListingContractTestMockData {
    fun outputState(testOwner : TestIdentity, testRequester: TestIdentity) : PropertyState
            = PropertyState(testOwner.party, testRequester.party,"A-24, New York",500,50)

    fun invalidOutputState(testOwner : TestIdentity) : PropertyState
            = PropertyState(testOwner.party, null,"A-24, New York",500,50)

    fun invalidPropertyAddressOutputState(testOwner : TestIdentity, testRequester: TestIdentity) : PropertyState
            = PropertyState(testOwner.party, testRequester.party,"", 500,50)

    fun invalidPropertyAreaOutputState(testOwner : TestIdentity, testRequester: TestIdentity) : PropertyState
            = PropertyState(testOwner.party, testRequester.party,"A-24, New York",-500,50)

    fun invalidPropertySellingPriceOutputState(testOwner : TestIdentity, testRequester: TestIdentity) : PropertyState
            = PropertyState(testOwner.party, testRequester.party,"A-24, New York",500,-50)
}