package lambdify.apigateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import java.util.Collections;
import lombok.val;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

class RequestRouterTest {

    Serializer jsonSerializer = new JsonSerializer();
    UserRepository userResource = Mockito.spy( UserRepository.class );
    RequestRouter urlRouter = new RequestRouter(
        Config.INSTANCE.defaultNotFoundHandler(),
        Collections.singletonList(jsonSerializer)
    );

    @DisplayName("Can match a single route")
    @Test void test0()
    {
        urlRouter.memorizeEndpoint(Methods.GET.and( "/users" ).withNoContent( userResource::retrieveUsers  ) );
        val endpoint = urlRouter.resolveRoute( request(Methods.GET, "/users" ) );
        endpoint.invoke( new Request() );
        verify(userResource).retrieveUsers( any() );
    }

    @DisplayName("Can match a single route with slash at end")
    @Test void test1()
    {
        urlRouter.memorizeEndpoint(Methods.GET.and( "/users" ).withNoContent( userResource::retrieveUsers  ) );
        val endpoint = urlRouter.resolveRoute( request(Methods.GET, "/users/" ) );
        endpoint.invoke( new Request() );
        verify(userResource).retrieveUsers( any() );
    }

    @DisplayName("Can match a route with a place holder")
    @Test void test2()
    {
        urlRouter.memorizeEndpoint( Methods.GET.and( "/users/:id" ).withNoContent( userResource::retrieveSingleUser  ) );
        val endpoint = urlRouter.resolveRoute( request(Methods.GET, "/users/1" ) );
        endpoint.invoke( new Request() );
        verify(userResource).retrieveSingleUser( any() );
    }

    @DisplayName("Can match a route with a place holder")
    @Test void test3()
    {
        urlRouter.memorizeEndpoint( Methods.GET.and( "/users/:id" ).withNoContent( userResource::retrieveSingleUser  ) );
        urlRouter.memorizeEndpoint( Methods.GET.and( "/users" ).withNoContent( userResource::retrieveUsers  ) );
        val endpoint = urlRouter.resolveRoute( request(Methods.GET, "/users/1" ) );
        endpoint.invoke( new Request() );
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

        assertEquals( 204, resp.statusCode );
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

        assertEquals( 201, resp.statusCode );
        verify( userResource ).createReportOfUsers();
    }

    @DisplayName("Can handle requests which requires the unserialized body")
    @Test void test6()
    {
        urlRouter.memorizeEndpoint(Methods.PUT.and( "/users" ).with( userResource::updateUser ) );

        val req = request(Methods.PUT, "/users" ).setBody( "{\"name\":\"Helden Liniel\"}" )
                .setHeaders( Collections.singletonMap("Content-Type", "application/json") );

        val resp = urlRouter.doRouting( req, null );
        assertEquals( 200, resp.statusCode );
        assertEquals( "Helden Liniel", resp.body );
    }

    @DisplayName("Endpoints can send objects and gets its content serialized at the response body")
    @Test void test7(){
        urlRouter.memorizeEndpoint(Methods.GET.and( "/users" ).with( userResource::retrieveUsers ) );
        val req = request(Methods.GET, "/users" );
        val resp = urlRouter.doRouting( req, null );
        assertEquals( "[{\"name\":\"User\"}]", resp.body );
    }

    Request request( Methods method, String url ) {
        val req = new Request();
        req.path = url;
        req.httpMethod = method.toString();
        return req;
    }
}
