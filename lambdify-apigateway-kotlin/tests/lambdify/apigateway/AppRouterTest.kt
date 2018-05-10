package lambdify.apigateway

import lambdify.apigateway.Methods.*
import lambdify.apigateway.kotlin.App
import lambdify.apigateway.kotlin.and
import lambdify.apigateway.kotlin.with
import lambdify.apigateway.kotlin.withNoContent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * Created by miere.teixeira on 06/04/2018.
 */
class AppRouterTest {

    class MyApp: App({

        val userResource = UserResource()

        Config.INSTANCE.defaultNotFoundHandler( userResource::customNotFoundHandler )

        routes(
            GET and "/users" with userResource::retrieveUsers,
            GET and "/users/:id" with userResource::retrieveSingleUser,
            PATCH and "/users" with userResource::doReportOfUsers,
            POST and "/users" withNoContent userResource::saveUser
        )
    })

    val app = MyApp()

    @DisplayName( "Can handle URLs that not matches any endpoint" )
    @Test fun test1(){
        val req = request("/groups/1", Methods.GET)
        val response = app.handleRequest(req, null)
        assertEquals(404, response.statusCode)
        assertEquals( "Not Found", response.headers!!["X-Custom"] )
    }

    @DisplayName( "Stress Test" )
    @Test fun test2(){
        val manyTimes = 1000000
        for ( i in 0..manyTimes) { test1() }
    }

    @DisplayName( "Can match an endpoint" )
    @Test fun test3(){
        val req = request("/users/1", Methods.GET)
        val response = app.handleRequest(req, null)
        assertEquals(200, response.statusCode)
        assertEquals( "{'name':'Lambda User'}", response.body )
    }

    fun request( url:String, method:Methods )
        = Request().apply {
            this.path = url
            this.httpMethod = method.toString()
        }
}

