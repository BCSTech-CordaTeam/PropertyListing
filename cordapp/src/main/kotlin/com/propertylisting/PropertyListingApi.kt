package com.propertylisting

import com.propertylisting.flows.PropertyRegistrationFlow
import com.propertylisting.flows.RequestPropertyListFlow.Initiator
import com.propertylisting.states.PropertyState
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.startTrackedFlow
import net.corda.core.utilities.getOrThrow
import org.eclipse.jetty.http.HttpStatus
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

val SERVICE_NAMES = listOf("Notary", "Network Map Service")

@Path("propertyListing")
class PropertyListingApi(private val rpcOps: CordaRPCOps) {
    private val myLegalName: CordaX500Name = rpcOps.nodeInfo().legalIdentities.first().name

    @GET
    @Path("myName")
    @Produces(MediaType.APPLICATION_JSON)
    fun myName() = mapOf("myName" to myLegalName)

    @GET
    @Path("peers")
    @Produces(MediaType.APPLICATION_JSON)
    fun peers() = mapOf("peers" to rpcOps.networkMapSnapshot()
            .map { it.legalIdentities.first().name }
            .filter { it.organisation !in (SERVICE_NAMES + myLegalName.organisation) })

    @POST
    @Path("registration")
    fun propertyRegistration(@QueryParam("propertyAddress") propertyAddress: String,
                             @QueryParam("propertyArea") propertyArea: Long,
                             @QueryParam("propertySellingPrice") propertySellingPrice: Long): Response
    {
        if(propertyAddress.isEmpty())
            return Response.status(HttpStatus.BAD_REQUEST_400).entity("Property Address is a mandatory field.").build()
        if(propertyArea <= 0)
            return Response.status(HttpStatus.BAD_REQUEST_400).entity("Property Area should be greater than zero.").build()
        if(propertySellingPrice <= 0)
            return Response.status(HttpStatus.BAD_REQUEST_400).entity("Property Selling Price should be greater than zero.").build()
        return try {
            val signedTx = rpcOps.startTrackedFlow(::PropertyRegistrationFlow, propertyAddress,propertyArea,propertySellingPrice).returnValue.getOrThrow()
            if(signedTx == null)
                Response.status(HttpStatus.CONFLICT_409).entity("Property with this address is already registered.").build()
            else
                Response.status(HttpStatus.CREATED_201).entity("Property is registered successfully.").build()
        } catch (ex: Throwable) {
            Response.status(HttpStatus.BAD_REQUEST_400).entity("Error in Property Registration : " + ex.message!!).build()
        }
    }

    @POST
    @Path("requestPropertyList")
    fun requestPropertyList(@QueryParam("owner") owner: CordaX500Name?) : Response {
        return try {
            if(owner == null) {
                return Response.status(HttpStatus.BAD_REQUEST_400).entity("Invalid input").build();
            }
            val ownerParty : Party = rpcOps.wellKnownPartyFromX500Name(owner)!!
            rpcOps.startTrackedFlow(::Initiator, ownerParty)
            Response.status(HttpStatus.OK_200).entity("Property List request is successful").build()
        } catch (ex: Throwable) {
            Response.status(HttpStatus.BAD_REQUEST_400).entity("Error in Property List request : " + ex.message!!).build()
        }
    }

    @GET
    @Path("ownedPropertyList")
    @Produces(MediaType.APPLICATION_JSON)
    fun ownedPropertyList() = rpcOps.vaultQuery(PropertyState::class.java).states
            .filter { (state) -> state.data.owner.name == myLegalName && state.data.requester?.name == null}

    @GET
    @Path("propertySellingPrice")
    @Produces(MediaType.APPLICATION_JSON)
    fun propertySellingPrice() = rpcOps.vaultQuery(PropertyState::class.java).states
            .filter { (state) -> state.data.owner.name == myLegalName && state.data.requester?.name == null}
            .map { (state) -> mapOf("Property Address" to state.data.propertyAddress, "Property Selling Price" to state.data.propertySellingPrice) }

    @GET
    @Path("propertyArea")
    @Produces(MediaType.APPLICATION_JSON)
    fun propertyArea() = rpcOps.vaultQuery(PropertyState::class.java).states
            .filter { (state) -> state.data.owner.name == myLegalName && state.data.requester?.name == null}
            .map { (state) -> mapOf("Property Address" to state.data.propertyAddress, "Property Area" to state.data.propertyArea) }

    @GET
    @Path("requestedProperties")
    @Produces(MediaType.APPLICATION_JSON)
    fun requestedProperties() = rpcOps.vaultQuery(PropertyState::class.java).states
            .filter { (state) -> state.data.requester?.name == myLegalName}
            .map { (state) -> mapOf("Owner" to state.data.owner , "Property Address" to state.data.propertyAddress)}

    @GET
    @Path("requestedPropertyDetailsByAddress")
    @Produces(MediaType.APPLICATION_JSON)
    fun requestedPropertyDetails(@QueryParam("address") address: String) = rpcOps.vaultQuery(PropertyState::class.java).states
            .filter { (state) -> state.data.requester?.name == myLegalName && state.data.propertyAddress.equals(address)}
            .map { (state) -> mapOf("Owner" to state.data.owner, "Property Address" to state.data.propertyAddress
                    ,"Property Area" to state.data.propertyArea, "Property Selling Price" to state.data.propertySellingPrice)}

    @GET
    @Path("sharedPropertyList")
    @Produces(MediaType.APPLICATION_JSON)
    fun sharedProperties() = rpcOps.vaultQuery(PropertyState::class.java).states
            .filter { (state) -> state.data.owner.name == myLegalName && state.data.requester?.name != null}
            .map { (state) -> mapOf("Property Address" to state.data.propertyAddress,  "Requester" to state.data.requester)}

    @GET
    @Path("sharedProperties")
    @Produces(MediaType.APPLICATION_JSON)
    fun sharedPropertiesToRequester(@QueryParam("requester") requester: String) = rpcOps.vaultQuery(PropertyState::class.java).states
            .filter { (state) -> state.data.owner.name == myLegalName && state.data.requester?.name.toString() == requester}
            .map { (state) -> mapOf("Property Address" to state.data.propertyAddress,  "Requester" to state.data.requester)}
}