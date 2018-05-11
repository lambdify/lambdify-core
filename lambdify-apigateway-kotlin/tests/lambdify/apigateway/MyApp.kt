package lambdify.apigateway

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
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

    Config.INSTANCE.defaultNotFoundHandler( users::customNotFoundHandler )

    routes(
        GET and "/users" with users::retrieveUsers,
        GET and "/users/:id" with users::retrieveSingleUser,
        PATCH and "/users" with users::doReportOfUsers,
        POST and "/users" withNoContent users::saveUser
    )
})

open class UserResource {

    lateinit var lastExecutedMethod:String

    fun customNotFoundHandler( request: APIGatewayProxyRequestEvent): APIGatewayProxyResponseEvent {
        lastExecutedMethod = "customNotFoundHandler"
        return Responses.notFound().apply {
            this.headers = mutableMapOf(
                "X-Custom" to "Not Found"
            )
        }
    }

    fun retrieveUsers( request: APIGatewayProxyRequestEvent): APIGatewayProxyResponseEvent {
        lastExecutedMethod = "retrieveUsers"
        return Responses.noContent()
    }

    fun retrieveSingleUser( request: APIGatewayProxyRequestEvent? ): APIGatewayProxyResponseEvent {
        lastExecutedMethod = "retrieveSingleUser"
        return Responses.ok("{'name':'Lambda User'}", "application/json")
    }

    fun doReportOfUsers(): APIGatewayProxyResponseEvent {
        lastExecutedMethod = "doReportOfUsers"
        return Responses.created()
    }

    fun saveUser( request: APIGatewayProxyRequestEvent? ) {
        lastExecutedMethod = "saveUser"
    }
}