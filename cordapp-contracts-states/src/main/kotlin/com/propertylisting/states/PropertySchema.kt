package com.propertylisting.states

import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

object PropertySchema

object PropertySchema1 : MappedSchema(
        schemaFamily = PropertySchema.javaClass,
        version = 1,
        mappedTypes = listOf(PropertyData::class.java)) {
    @Entity
    @Table(name = "PropertyState")
    class PropertyData(
        @Column(name = "owner_name")
        var ownerName: String,

        @Column(name = "requester_name")
        var requesterName: String,

        @Column(name = "property_address")
        var propertyAddress: String,

        @Column(name = "property_area")
        var propertyArea: Long,

        @Column(name = "property_selling_price")
        var propertySellingPrice: Long,

        @Column(name = "property_id")
        var linearId: UUID
    ) : PersistentState() {
        constructor(): this("","","",0L,0L,UUID.randomUUID())
    }
}