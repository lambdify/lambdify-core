package lambdify.apigateway.apt;

import static lambdify.apigateway.Methods.*;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import lambdify.apigateway.Responses;
import lambdify.apigateway.ann.*;
import lambdify.aws.events.apigateway.*;

/**
 *
 */
public class MyAnnotatedResource {

	final AtomicBoolean semaphore;

	public MyAnnotatedResource( AtomicBoolean semaphore ) {
		this.semaphore = semaphore;
	}

	@Route( url = "/api/users/:id", method = GET )
	User getUser(@PathParam("id") Long id, @HeaderParam("accept") String accept, @QueryParam("page") String page, ProxyRequestEvent request ){
		return new User( id );
	}

	@Route( url = "/api/users", method = POST)
	@ContentType("application/json")
	void saveUser( @Context Account account, @Body User user ){

	}

	@Route( url = "/api/users", method = PATCH)
	@ContentType("application/json")
	long saveUser( @Body Map<String, Object> user ){
		return System.currentTimeMillis(); // fake generated id.
	}

	@Route( url = "/api/users", method = PUT)
	ProxyResponseEvent updateUser(@QueryParam( "id" ) Long id, User user ){
		return Responses.accepted();
	}
}

class User {

	long id;

	User( long id ) {
		this.id = id;
	}
}