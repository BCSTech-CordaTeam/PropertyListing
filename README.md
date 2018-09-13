# Property Listing CorDapp - Queryable States

Property Listing CorDapp provides the property owner with a functionality to upload the details(address, area, selling price) of the property that he wants to sell.
A potential buyer node uses a **_pull mechanism_** to request other nodes in the network for a list of houses/properties that the node has. 
In this example, we are storing all properties returned by the sender node however, this pull mechanism allows the buyer node to decide what they want to store in the vault after verifying the properties shared by the sender node. 


This CorDapp illustrates the power of an RDBMS running on a distributed ledger.
By letting the ContractState implement the **_QueryableState_** interface, a Corda node’s vault can become SQL queryable with any JDBC friendly database in the world.

The Property Listing CorDapp provides examples of querying the vault based on State attributes via both the **_ServiceHub_** and **_CordaRPCOps_**.

Corda documentation links for reference:
* [Persistence](https://docs.corda.net/api-persistence.html)
* [Vault Query](https://docs.corda.net/api-vault-query.html)

## Pre-Requisites

You will need the following installed on your machine before you can start:

* [JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) 
  installed and available on your path (Minimum version: 1.8_131).
* [IntelliJ IDEA](https://www.jetbrains.com/idea/download/) (Minimum version 2017.1)
* git
* Optional: [h2 web console](http://www.h2database.com/html/download.html)
  (download the "platform-independent zip")

For more detailed information, see the
[getting set up](https://docs.corda.net/getting-set-up.html) page on the
Corda docsite.

## Getting Set Up

To get started, clone this repository with:

     git clone https://github.com/BCSTech-CordaTeam/PropertyListing.git

And change directories to the newly cloned repo:

     cd PropertyListing

## Building the CorDapp

**Unix:** 

     ./gradlew deployNodes

**Windows:**

     gradlew.bat deployNodes


## Running the Nodes

     cd build/nodes

**Unix:**

     ./runnodes

**Windows:**

    runnodes.bat

## Interacting with the CorDapp using node shell

**Property Registration** - Initiated by property owner

    flow start PropertyRegistrationFlow propertyAddress: "A-24, New York", propertyArea: 400, propertySellingPrice: 50

**Request Property List** - Initiated by potential property buyer(requester)

    flow start RequestPropertyListFlow owner: "O=PartyA,L=London,C=GB"

## Interacting with the CorDapp via HTTP

Following API endpoints are available:

* Retrieve name of node

    ```
    /api/propertyListing/myName
    ```
* Retrieve list of peer nodes

    ```
    /api/propertyListing/peers
    ```
* Property Registration initiated by owner

    ```
    /api/propertyListing/registration
    ```

    Example:

    ```
    /api/propertyListing/registration?propertyAddress=A-24, New York&propertyArea=500&propertySellingPrice=90
    ```

*  Retrieve list of properties owned by the node

    ```
    /api/propertyListing/ownedPropertyList
    ```
    
* Retrieve list of {property address , property selling price} of properties owned by the node
    
    ```
    /api/propertyListing/propertySellingPrice
    ```
    
*   Retrieve list of {property address , property area} of properties owned by the node
    
    ```
    /api/propertyListing/propertyArea 
    ```

*   Request property list from owner
    
    ```
    /api/propertyListing/requestPropertyList 
    ```

    Example:

    ```
    /api/propertyListing/requestPropertyList?owner=O=PartyA,L=London,C=GB
    ```

*   Retrieve list of properties that the node requested from property owners
    
    ```
    /api/propertyListing/requestedProperties
    ```
    
*   Retrieve details of a particular requested property
    
    ```
    /api/propertyListing/requestedPropertyDetailsByAddress
    ```

    Example:

    ```
    /api/propertyListing/requestedPropertyDetailsByAddress?address=A-24, New York
    ```

*   Retrieve list of properties that the node shared with requesters
    
    ```
    /api/propertyListing/sharedPropertyList
    ```
    
*   Retrieve list of properties that the node shared with a particular requester

    ```
    /api/propertyListing/sharedProperties
    ```

    Example:

    ```
    /api/propertyListing/sharedProperties?requester=O=PartyB, L=New York, C=US
    ```

### Postman Collection

The postman collection and environment json files are in the following directory:

    /cordapp/src/main/resources/postman

