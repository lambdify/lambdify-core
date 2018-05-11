package lambdify.apigateway;

import static java.util.Collections.singletonMap;
import java.util.Map;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import lombok.val;

/**
 *
 */
public interface Responses {

	static APIGatewayProxyResponseEvent notFound() {
		return new APIGatewayProxyResponseEvent().withStatusCode( 404 );
	}

	static APIGatewayProxyResponseEvent noContent() {
		return new APIGatewayProxyResponseEvent().withStatusCode( 204 );
	}

	static APIGatewayProxyResponseEvent created() {
		return new APIGatewayProxyResponseEvent().withStatusCode( 201 );
	}

	static APIGatewayProxyResponseEvent accepted() {
		return new APIGatewayProxyResponseEvent().withStatusCode( 202 );
	}

	static APIGatewayProxyResponseEvent ok(String body) {
		return ok( body, Config.INSTANCE.defaultContentType() );
	}

	static APIGatewayProxyResponseEvent ok(String body, String contentType) {
		val contentTypeHeaders = singletonMap( "Content-Type", contentType );
		return new APIGatewayProxyResponseEvent().withStatusCode( 200 ).withBody( body ).withHeaders( contentTypeHeaders );
	}

	static APIGatewayProxyResponseEvent ok(Object body) {
		return ok( body, Config.INSTANCE.defaultContentType() );
	}

	static APIGatewayProxyResponseEvent ok(Object body, String contentType) {
		val serializer = getResponseSerializer(contentType);
		val serialized = serializer.toString( body );
		val contentTypeHeaders = singletonMap( "Content-Type", contentType );
		return new APIGatewayProxyResponseEvent().withStatusCode( 200 )
				.withBody( serialized.getContent() ).withHeaders( contentTypeHeaders );
	}

	static Serializer getResponseSerializer( String contentType ) {
		if ( contentType == null || contentType.isEmpty() ) {
			contentType = Config.INSTANCE.defaultContentType();
			System.out.println( "No content type defined. Using default: " + contentType );
		}
		val serializer = Config.INSTANCE.serializers().get( contentType );
		if ( serializer == null )
			throw new RuntimeException( "Could not generate a response: no serializer found for " + contentType );
		return serializer;
	}

	static APIGatewayProxyResponseEvent with(int statusCode, Map<String, String> headers, String body) {
		return new APIGatewayProxyResponseEvent()
				.withStatusCode( statusCode )
				.withHeaders( headers )
				.withBody( body );
	}

	static APIGatewayProxyResponseEvent internalServerError(String errorMessage ) {
		val contentTypeHeaders = singletonMap( "Content-Type", "text/plain" );
		return new APIGatewayProxyResponseEvent().withStatusCode( 500 ).withBody( errorMessage )
				.withHeaders( contentTypeHeaders );
	}
}
