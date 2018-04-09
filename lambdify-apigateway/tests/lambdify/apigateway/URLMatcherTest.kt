package lambdify.apigateway

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * Created by miere.teixeira on 07/04/2018.
 */
class URLMatcherTest {

    val userResource = UserResource()
    val notFound = { _: APIGatewayRequest -> APIGatewayResponse.notFound() }
    val urlMatcher = UrlMatcher(notFound)

    @DisplayName("Can match a single route")
    @Test fun test0()
    {
        urlMatcher.memorizeEndpoint(Methods.GET and "/users" with userResource::retrieveUsers )
        val endpoint = urlMatcher.matchRoute( request(Methods.GET, "/users" ) )
        assertEquals( userResource::retrieveUsers, endpoint )
    }

    @DisplayName("Can match a single route with slash at end")
    @Test fun test1()
    {
        urlMatcher.memorizeEndpoint(Methods.GET and "/users" with userResource::retrieveUsers )
        val endpoint = urlMatcher.matchRoute( request(Methods.GET, "/users/" ) )
        assertEquals( userResource::retrieveUsers, endpoint )
    }

    @DisplayName("Can match a route with a place holder")
    @Test fun test2()
    {
        urlMatcher.memorizeEndpoint( Methods.GET and "/users/:id" with userResource::retrieveSingleUser )

        val endpoint = urlMatcher.matchRoute( request(Methods.GET, "/users/1" ) )
        assertEquals( userResource::retrieveSingleUser, endpoint )
    }

    @DisplayName("Can match a route with a place holder")
    @Test fun test3()
    {
        urlMatcher.memorizeEndpoint( Methods.GET and "/users/:id" with userResource::retrieveSingleUser )
        urlMatcher.memorizeEndpoint( Methods.GET and "/users" with userResource::retrieveUsers )

        val endpoint = urlMatcher.matchRoute( request(Methods.GET, "/users/1" ) )
        assertEquals( userResource::retrieveSingleUser, endpoint )
    }

    @DisplayName("Can match an endpoint with no return type defined")
    @Test fun test4()
    {
        urlMatcher.memorizeEndpoint( Methods.GET and "/users/:id" with userResource::retrieveSingleUser )
        urlMatcher.memorizeEndpoint( Methods.GET and "/users" with userResource::retrieveUsers )
        urlMatcher.memorizeEndpoint( Methods.POST and "/users" withNoResponse userResource::saveUser )

        val req = request(Methods.POST, "/users/" )
        val endpoint = urlMatcher.matchRoute( req )
        val resp = endpoint.invoke( req )

        assertEquals( 204, resp.statusCode )
        assertEquals( "saveUser", userResource.lastExecutedMethod )
    }

    @DisplayName("Can match an endpoint with no argument defined")
    @Test fun test5()
    {
        urlMatcher.memorizeEndpoint(Methods.GET and "/users/:id" with userResource::retrieveSingleUser )
        urlMatcher.memorizeEndpoint(Methods.GET and "/users" withNoArgs userResource::doReportOfUsers )
        urlMatcher.memorizeEndpoint(Methods.POST and "/users" withNoResponse userResource::saveUser )

        val req = request(Methods.GET, "users" )
        val endpoint = urlMatcher.matchRoute( req )
        val resp = endpoint.invoke( req )

        assertEquals( 201, resp.statusCode )
        assertEquals( "doReportOfUsers", userResource.lastExecutedMethod )
    }

    fun request(method: Methods, url:String ) =
        APIGatewayRequest().apply {
            this.path = url
            this.httpMethod = method.toString()
        }
}
