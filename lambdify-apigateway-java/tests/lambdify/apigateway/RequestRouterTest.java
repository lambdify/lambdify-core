package lambdify.apigateway;

import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import java.util.Collections;
import lambdify.aws.events.apigateway.ProxyRequestEvent;
import lombok.val;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

class RequestRouterTest {

    UserRepository userResource = Mockito.spy( UserRepository.class );
    RequestRouter urlRouter = new RequestRouter();

    @BeforeEach
    void configureSerializers(){
        val jsonSerializer = new JsonSerializer();
        ApiGatewayConfig.INSTANCE.registerSerializer( jsonSerializer );
    }

    @DisplayName("Can match a single route")
    @Test void test0()
    {
        urlRouter.memorizeEndpoint(Methods.GET.and( "/users" ).withNoContent( userResource::retrieveUsers  ) );
        val endpoint = urlRouter.resolveRoute( request(Methods.GET, "/users" ) );
        endpoint.invoke( new ProxyRequestEvent() );
        verify(userResource).retrieveUsers( any() );
    }

    @DisplayName("Can match a single route with slash at end")
    @Test void test1()
    {
        urlRouter.memorizeEndpoint(Methods.GET.and( "/users" ).withNoContent( userResource::retrieveUsers  ) );
        val endpoint = urlRouter.resolveRoute( request(Methods.GET, "/users/" ) );
        endpoint.invoke( new ProxyRequestEvent() );
        verify(userResource).retrieveUsers( any() );
    }

    @DisplayName("Can match a route with a place holder")
    @Test void test2()
    {
        urlRouter.memorizeEndpoint( Methods.GET.and( "/users/sub/:id" ).withNoContent( userResource::retrieveSingleUser  ) );
        val request = request(Methods.GET, "/users/sub/123" );
        val endpoint = urlRouter.resolveRoute( request );
        assertTrue( request.getPathParameters().containsKey( "id" ) );
        assertEquals( "123", request.getPathParameters().get("id") );

        endpoint.invoke( new ProxyRequestEvent() );
        verify(userResource).retrieveSingleUser( any() );
    }

    @DisplayName("Can match a route with a place holder")
    @Test void test3()
    {
        urlRouter.memorizeEndpoint( Methods.GET.and( "/users/:id" ).withNoContent( userResource::retrieveSingleUser  ) );
        urlRouter.memorizeEndpoint( Methods.GET.and( "/users" ).withNoContent( userResource::retrieveUsers  ) );
        val endpoint = urlRouter.resolveRoute( request(Methods.GET, "/users/1" ) );
        endpoint.invoke( new ProxyRequestEvent() );
        verify(userResource).retrieveSingleUser( any() );
    }

    @DisplayName("Can match an endpoint with no return type defined")
    @Test void test4()
    {
        urlRouter.memorizeEndpoint( Methods.GET.and( "/users/:id" ).withNoContent( userResource::retrieveSingleUser  ) );
        urlRouter.memorizeEndpoint( Methods.GET.and( "/users" ).withNoContent( userResource::retrieveUsers  ) );
        urlRouter.memorizeEndpoint( Methods.POST.and( "/users" ).withNoContent( userResource::saveUser  ) );

        val req = request(Methods.POST, "/users/" );
        val endpoint = urlRouter.resolveRoute( req );
        val resp = endpoint.invoke( req );

        assertEquals( 204, (int)resp.getStatusCode() );
        verify( userResource ).saveUser( any() );
    }

    @DisplayName("Can match an endpoint with no argument defined")
    @Test void test5()
    {
        urlRouter.memorizeEndpoint(Methods.GET.and( "/users/:id" ).withNoContent( userResource::retrieveSingleUser  ) );
        urlRouter.memorizeEndpoint(Methods.GET.and( "/users" ).with( userResource::createReportOfUsers ) );
        urlRouter.memorizeEndpoint(Methods.POST.and( "/users" ).withNoContent( userResource::saveUser  ) );

        val req = request(Methods.GET, "users" );
        val endpoint = urlRouter.resolveRoute( req );
        val resp = endpoint.invoke( req );

        assertEquals( 201, (int)resp.getStatusCode() );
        verify( userResource ).createReportOfUsers();
    }

    @DisplayName("Can handle requests which requires the unserialized body")
    @Test void test6()
    {
        urlRouter.memorizeEndpoint(Methods.PUT.and( "/users" ).with( userResource::updateUser ) );

        val req = request(Methods.PUT, "/users" )
		        .withBody( "{\"name\":\"Helden Liniel\"}" )
		        .withHeaders( singletonMap("Content-Type", "application/json") );

        val resp = urlRouter.doRouting( req, null );
        assertEquals( 200, (int)resp.getStatusCode() );
        assertEquals( "Helden Liniel", resp.getBody() );
    }

    @DisplayName("Endpoints can send objects and gets its content serialized at the response body")
    @Test void test7(){
        urlRouter.memorizeEndpoint(Methods.GET.and( "/users" ).with( userResource::retrieveUsers ) );
        val req = request(Methods.GET, "/users" );
        val resp = urlRouter.doRouting( req, null );
        assertEquals( "[{\"name\":\"User\"}]", resp.getBody() );
    }

    @DisplayName("Can match the root URL in cases where a 'slash' plus 'place holder' exists")
    @Test void test8(){
        urlRouter.memorizeEndpoint(Methods.GET.and( "/" ).with( userResource::retrieveUsers ) );
        urlRouter.memorizeEndpoint(Methods.GET.and( "/:any" ).with( userResource::createReportOfUsers ) );
        val req = request(Methods.GET, "/" );
        val resp = urlRouter.doRouting( req, null );
        assertEquals( "[{\"name\":\"User\"}]", resp.getBody() );
    }

    ProxyRequestEvent request(Methods method, String url ) {
        val req = new ProxyRequestEvent();
        req.setPath( url );
        req.setHeaders( Collections.emptyMap() );
        req.setHttpMethod( method.toString() );
        return req;
    }
}
