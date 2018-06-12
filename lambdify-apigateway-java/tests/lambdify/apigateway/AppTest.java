package lambdify.apigateway;

import static lambdify.apigateway.Defaults.createRequest;
import static lambdify.apigateway.Methods.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import com.amazonaws.services.lambda.runtime.*;
import lombok.*;
import org.junit.jupiter.api.*;

class AppTest {

	private Context context = new DefaultAwsContext();
    private App app;

    @BeforeEach void setupApp()
    {
        val userResource = new UserRepository();

        ApiGatewayConfig.INSTANCE.defaultNotFoundHandler( userResource::customNotFoundHandler );

        app = new App(){{
            routes(
                GET.and("/users").with(userResource::retrieveUsers),
                GET.and("/users/:id").with(userResource::retrieveSingleUser),
                PATCH.and("/users").with(userResource::createReportOfUsers),
                POST.and("/users").withNoContent(userResource::saveUser)
            );
        }};
    }

    @DisplayName( "Can handle URLs that not matches any endpoint" )
    @Test void test1(){
        val req = createRequest( "/profiles/1", Methods.GET );
        val response = app.handleRequest( req, context);
        assertEquals(404, (int)response.getStatusCode() );
        assertEquals( "Not Found", response.getHeaders().get("X-Custom") );
    }

    @DisplayName( "Stress Test" )
    @Test void test2()
    {
        Runnable method = () -> {
            val req = createRequest( "/profiles/1", Methods.GET );
            val response = app.handleRequest( req, context);
            assertEquals(404, (int)response.getStatusCode() );
            assertEquals( "Not Found", response.getHeaders().get("X-Custom") );
        };

        val manyTimes = 10000000;
        for ( int i=0; i<manyTimes; i++ ) {
            method.run();
        }
    }

    @DisplayName( "Can match an endpoint" )
    @Test void test3(){
        val req = createRequest( "/users/1", Methods.GET );
        val response = app.handleRequest(req, context);
        assertEquals(200, (int)response.getStatusCode() );
        assertEquals( "{'name':'Lambda User'}", response.getBody() );
    }

    @Data class DefaultAwsContext implements Context {
    	String
		    awsRequestId,
		    logGroupName, logStreamName,
		    functionName, functionVersion, invokedFunctionArn;

	    int remainingTimeInMillis, memoryLimitInMB;

	    CognitoIdentity identity;
	    ClientContext clientContext;

	    LambdaLogger logger = System.err::println;
    }
}
