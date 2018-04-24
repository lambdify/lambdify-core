package lambdify.apigateway

import lambdify.apigateway.Methods.*
import lambdify.apigateway.kotlin.App
import lambdify.apigateway.kotlin.*

/**
 * Created by miere.teixeira on 06/04/2018.
 */
class MyApp: App({

    val users = UserResource()

    notFoundHandler( users::customNotFoundHandler )

    routes(
        GET and "/users" with users::retrieveUsers,
        GET and "/users/:id" with users::retrieveSingleUser,
        PATCH and "/users" with users::doReportOfUsers,
        POST and "/users" withNoContent users::saveUser
    )
})

open class UserResource {

    lateinit var lastExecutedMethod:String

    fun customNotFoundHandler( request: APIGateway.Request ): APIGateway.Response {
        lastExecutedMethod = "customNotFoundHandler"
        return APIGateway.Response.notFound().apply {
            this.headers = mutableMapOf(
                "X-Custom" to "Not Found"
            )
        }
    }

    fun retrieveUsers( request: APIGateway.Request): APIGateway.Response {
        lastExecutedMethod = "retrieveUsers"
        return APIGateway.Response.noContent()
    }

    fun retrieveSingleUser( request: APIGateway.Request? ): APIGateway.Response {
        lastExecutedMethod = "retrieveSingleUser"
        return APIGateway.Response.ok("{'name':'Lambda User'}", "application/json")
    }

    fun doReportOfUsers(): APIGateway.Response {
        lastExecutedMethod = "doReportOfUsers"
        return APIGateway.Response.created()
    }

    fun saveUser( request: APIGateway.Request? ) {
        lastExecutedMethod = "saveUser"
    }
}