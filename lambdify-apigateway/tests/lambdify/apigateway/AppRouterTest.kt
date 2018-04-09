package lambdify.apigateway

import lambdify.apigateway.Methods.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * Created by miere.teixeira on 06/04/2018.
 */
class AppRouterTest {

    private val userResource = UserResource()
    private val app = App({

        notFoundHandler = userResource::customNotFoundHandler

        routes(
                GET and "/users" with userResource::retrieveUsers,
                GET and "/users/:id" with userResource::retrieveSingleUser,
                PATCH and "/users" withNoArgs userResource::doReportOfUsers,
                POST and "/users" withNoResponse userResource::saveUser
        )
    })

    @DisplayName( "Can handle URLs that not matches any endpoint" )
    @Test fun test1(){
        val response = app.handleRequest(APIGatewayRequest(), null)
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
        val req = APIGatewayRequest().apply { path = "/users/1"; httpMethod = Methods.GET.toString() }
        val response = app.handleRequest(req, null)
        assertEquals(200, response.statusCode)
        assertEquals( "{'name':'Lambda User'}", response.body )
    }
}

