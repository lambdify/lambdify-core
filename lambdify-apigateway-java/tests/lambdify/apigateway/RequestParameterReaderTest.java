package lambdify.apigateway;

import static lambdify.apigateway.Methods.GET;
import static org.junit.jupiter.api.Assertions.assertEquals;
import lambdify.aws.events.apigateway.ProxyRequestEvent;
import lombok.val;
import org.junit.jupiter.api.*;

/**
 *
 */
class RequestParameterReaderTest {

	final String JSON = "{}";

	final ProxyRequestEvent defaultRequest = Defaults
			.createRequest( "/", GET ).withBody( JSON );

	@DisplayName( "Can read body as String" )
	@Test void getBodyAs0() {
		val body = RequestParameterReader.getBodyAs( defaultRequest, String.class );
		assertEquals( JSON, body );
	}

	@DisplayName( "Can read body as byte[]" )
	@Test void getBodyAs1() {
		val body = RequestParameterReader.getBodyAs( defaultRequest, byte[].class );
		assertEquals( JSON, new String(body) );
	}

}