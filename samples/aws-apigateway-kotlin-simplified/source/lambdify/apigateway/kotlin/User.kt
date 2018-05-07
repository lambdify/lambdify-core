package lambdify.apigateway.kotlin

import lambdify.apigateway.APIGateway

/**
 *
 */
class UserResource {

    val data = mutableMapOf<String,String>()

    fun saveUser( request: APIGateway.Request){
        val id = request.pathParameters!!["id"]!!
        data[id] = request.body!!
    }

    fun retrieveUsers() = APIGateway.Response.ok(data.values.joinToString { it })

    fun retrieveUser( req: APIGateway.Request): APIGateway.Response {
        val id = req.pathParameters!!["id"]!!
        val user = retrieveUserById(id)
        return APIGateway.Response.ok( user );
    }

    fun retrieveUserById( id:String ):String? {
        return data[ id ]
    }

    fun root() = APIGateway.Response.ok("Hello World")
}