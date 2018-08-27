package com.propertylisting.states

import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState

data class PropertyState(
        val owner: Party,
        val requester: Party ?= null ,
        val propertyAddress: String,
        val propertyArea: Long,
        val propertySellingPrice: Long,
        override val linearId: UniqueIdentifier =UniqueIdentifier()) : LinearState, QueryableState {

    override val participants: List<AbstractParty> get() = if (requester!=null) listOf(owner,requester) else listOf(owner)

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is PropertySchema1 -> PropertySchema1.PropertyData(
                    this.owner.name.toString(),
                    this.requester?.name.toString(),
                    this.propertyAddress,
                    this.propertyArea,
                    this.propertySellingPrice,
                    this.linearId.id
            )
            else -> throw IllegalArgumentException("The schema : $schema does not exist.")
        }
    }

    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(PropertySchema1)
}
