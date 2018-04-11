package lambdify.apigateway

import lambdify.apigateway.Methods.*

/**
 * Created by miere.teixeira on 06/04/2018.
 */
class MyApp: APIGatewayApp({

    val users = UserResource()

    notFoundHandler = users::customNotFoundHandler

    routes(
        GET and "/users" with users::retrieveUsers,
        GET and "/users/:id" with users::retrieveSingleUser,
        PATCH and "/users" withNoArgs users::doReportOfUsers,
        POST and "/users" withNoResponse users::saveUser
    )
})

open class UserResource {

    lateinit var lastExecutedMethod:String

    fun customNotFoundHandler( request: APIGatewayRequest? ): APIGatewayResponse {
        lastExecutedMethod = "customNotFoundHandler"
        return APIGatewayResponse.notFound().apply {
            this.headers = mutableMapOf(
                "X-Custom" to "Not Found"
            )
        }
    }

    fun retrieveUsers( request: APIGatewayRequest): APIGatewayResponse {
        lastExecutedMethod = "retrieveUsers"
        return APIGatewayResponse.noContent()
    }

    fun retrieveSingleUser( request: APIGatewayRequest? ): APIGatewayResponse {
        lastExecutedMethod = "retrieveSingleUser"
        return APIGatewayResponse.ok("{'name':'Lambda User'}")
    }

    fun doReportOfUsers(): APIGatewayResponse {
        lastExecutedMethod = "doReportOfUsers"
        return APIGatewayResponse.created()
    }

    fun saveUser( request: APIGatewayRequest? ) {
        lastExecutedMethod = "saveUser"
    }
}