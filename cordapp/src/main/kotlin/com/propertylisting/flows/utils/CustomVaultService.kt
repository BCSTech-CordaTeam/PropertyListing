package com.propertylisting.flows.utils

import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.serialization.SingletonSerializeAsToken
import java.sql.PreparedStatement
import java.sql.ResultSet

object CustomVaultService {
    @CordaService
    class Service(val services: AppServiceHub) : SingletonSerializeAsToken() {
        fun getVacantProperties(owner: String): MutableList<Property> {
            val propertyList: MutableList<Property> = mutableListOf()
            val query = """
                        SELECT
                            state.property_address,
                            state.property_area,
                            state.property_selling_price,
                            state.property_id
                        FROM
                            PropertyState as state
                        WHERE
                            state.owner_name = ?
                        """
            val session = services.jdbcSession()
            val pstmt: PreparedStatement = session.prepareStatement(query)
            pstmt.setObject(1, owner)
            val rs: ResultSet = pstmt.executeQuery()
            while (rs.next()) {
                val propertyAddress = rs.getString("property_address")
                val propertyArea = rs.getLong("property_area")
                val propertySellingPrice = rs.getLong("property_selling_price")
                val property = Property(propertyAddress, propertyArea, propertySellingPrice)
                propertyList.add(property)
            }
            return propertyList
        }

        fun removeExistingProperties(owner: String, propertyList: MutableList<Property>): MutableList<Property>{
            val filteredPropertyList: MutableList<Property> = mutableListOf()
            for(property in propertyList){
                val query = """
                            SELECT
                                state.property_address
                            FROM
                                PropertyState as state
                            WHERE
                                state.owner_name = ?
                                AND
                                state.property_address = ?
                            """
                val session = services.jdbcSession()
                val pstmt: PreparedStatement = session.prepareStatement(query)
                pstmt.setObject(1, owner)
                pstmt.setObject(2, property.propertyAddress)
                val rs: ResultSet = pstmt.executeQuery()
                if(!rs.next())
                    filteredPropertyList.add(property)
            }
            return filteredPropertyList
        }

        fun checkPropertyExistence(owner: String, propertyAddress: String): Boolean{
            val query = """
                        SELECT
                            state.property_address
                        FROM
                            PropertyState as state
                        WHERE
                            state.owner_name = ?
                            AND
                            state.property_address = ?
                        """
            val session = services.jdbcSession()
            val pstmt: PreparedStatement = session.prepareStatement(query)
            pstmt.setObject(1, owner)
            pstmt.setObject(2, propertyAddress)
            val rs: ResultSet = pstmt.executeQuery()
            return (rs.next())
        }
    }
}