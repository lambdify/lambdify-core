package lambdify.apigateway;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static lambdify.apigateway.Methods.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AppTest {

    private UserRepository userResource = new UserRepository();
    private App app = new App(){{

        notFoundHandler( userResource::customNotFoundHandler );

        routes(
            GET.and("/users").with(userResource::retrieveUsers),
            GET.and("/users/:id").with(userResource::retrieveSingleUser),
            PATCH.and("/users").with(userResource::createReportOfUsers),
            POST.and("/users").withNoContent(userResource::saveUser)
        );
    }};

    @DisplayName( "Can handle URLs that not matches any endpoint" )
    @Test void test1(){
        val req = createRequest( "/profiles/1", Methods.GET );
        val response = app.handleRequest( req, null);
        assertEquals(404, response.statusCode);
        assertEquals( "Not Found", response.headers.get("X-Custom") );
    }

    @DisplayName( "Stress Test" )
    @Test void test2()
    {
        Runnable method = () -> {
            val req = createRequest( "/profiles/1", Methods.GET );
            val response = app.handleRequest( req, null);
            assertEquals(404, response.statusCode);
            assertEquals( "Not Found", response.headers.get("X-Custom") );
        };

        val manyTimes = 10000000;
        for ( int i=0; i<manyTimes; i++ ) {
            method.run();
        }
    }

    @DisplayName( "Can match an endpoint" )
    @Test void test3(){
        val req = createRequest( "/users/1", Methods.GET );
        val response = app.handleRequest(req, null);
        assertEquals(200, response.statusCode);
        assertEquals( "{'name':'Lambda User'}", response.body );
    }

    APIGateway.Request createRequest( String path, Methods method ) {
        return new APIGateway.Request()
            .setPath( path ).setHttpMethod( method.toString() );
    }
}
