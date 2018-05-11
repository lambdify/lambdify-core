package lambdify.apigateway.apt;

import static lambdify.apigateway.Methods.*;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import com.amazonaws.services.lambda.runtime.events.*;
import lambdify.apigateway.Responses;
import lambdify.apigateway.ann.*;

/**
 *
 */
public class MyAnnotatedResource {

	final AtomicBoolean semaphore;

	public MyAnnotatedResource( AtomicBoolean semaphore ) {
		this.semaphore = semaphore;
	}

	@Route( url = "/api/users/:id", method = GET )
	User getUser(@PathParam("id") Long id, APIGatewayProxyRequestEvent request ){
		return new User( id );
	}

	@Route( url = "/api/users", method = POST)
	@ContentType("application/json")
	void saveUser( @Body User user ){

	}

	@Route( url = "/api/users", method = PATCH)
	@ContentType("application/json")
	long saveUser( @Body Map<String, Object> user ){
		return System.currentTimeMillis(); // fake generated id.
	}

	@Route( url = "/api/users", method = PUT)
	APIGatewayProxyResponseEvent updateUser(@QueryParam( "id" ) Long id, User user ){
		return Responses.accepted();
	}
}

class User {

	long id;

	User( long id ) {
		this.id = id;
	}
}