package lambdify.apigateway

import lambdify.apigateway.Methods.*
import lambdify.apigateway.kotlin.App
import lambdify.apigateway.kotlin.and
import lambdify.apigateway.kotlin.with
import lambdify.apigateway.kotlin.withNoContent
import lambdify.aws.events.apigateway.ProxyRequestEvent
import lambdify.aws.events.apigateway.ProxyResponseEvent

/**
 * Created by miere.teixeira on 06/04/2018.
 */
class MyApp: App({

    val users = UserResource()

    ApiGatewayConfig.INSTANCE.defaultNotFoundHandler( users::customNotFoundHandler )

    routes(
        GET and "/users" with users::retrieveUsers,
        GET and "/users/:id" with users::retrieveSingleUser,
        PATCH and "/users" with users::doReportOfUsers,
        POST and "/users" withNoContent users::saveUser
    )
})

open class UserResource {

    lateinit var lastExecutedMethod:String

    fun customNotFoundHandler( request: ProxyRequestEvent): ProxyResponseEvent {
        lastExecutedMethod = "customNotFoundHandler"
        return Responses.notFound().apply {
            this.headers = mutableMapOf(
                "X-Custom" to "Not Found"
            )
        }
    }

    fun retrieveUsers( request: ProxyRequestEvent): ProxyResponseEvent {
        lastExecutedMethod = "retrieveUsers"
        return Responses.noContent()
    }

    fun retrieveSingleUser( request: ProxyRequestEvent? ): ProxyResponseEvent {
        lastExecutedMethod = "retrieveSingleUser"
        return Responses.ok("{'name':'Lambda User'}", "application/json")
    }

    fun doReportOfUsers(): ProxyResponseEvent {
        lastExecutedMethod = "doReportOfUsers"
        return Responses.created()
    }

    fun saveUser( request: ProxyRequestEvent? ) {
        lastExecutedMethod = "saveUser"
    }
}