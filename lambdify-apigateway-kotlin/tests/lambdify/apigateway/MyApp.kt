package lambdify.apigateway

import lambdify.apigateway.Methods.*
import lambdify.apigateway.kotlin.App
import lambdify.apigateway.kotlin.and
import lambdify.apigateway.kotlin.with
import lambdify.apigateway.kotlin.withNoContent

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

    fun customNotFoundHandler( request: Request ): Response {
        lastExecutedMethod = "customNotFoundHandler"
        return Response.notFound().apply {
            this.headers = mutableMapOf(
                "X-Custom" to "Not Found"
            )
        }
    }

    fun retrieveUsers( request: Request): Response {
        lastExecutedMethod = "retrieveUsers"
        return Response.noContent()
    }

    fun retrieveSingleUser( request: Request? ): Response {
        lastExecutedMethod = "retrieveSingleUser"
        return Response.ok("{'name':'Lambda User'}", "application/json")
    }

    fun doReportOfUsers(): Response {
        lastExecutedMethod = "doReportOfUsers"
        return Response.created()
    }

    fun saveUser( request: Request? ) {
        lastExecutedMethod = "saveUser"
    }
}