package lambdify.apigateway;

import static java.util.Collections.singletonMap;
import java.util.Map;
import lambdify.aws.events.apigateway.ProxyResponseEvent;
import lombok.val;

/**
 *
 */
public interface Responses {

	static ProxyResponseEvent notFound() {
		return new ProxyResponseEvent().withStatusCode( 404 );
	}

	static ProxyResponseEvent noContent() {
		return new ProxyResponseEvent().withStatusCode( 204 );
	}

	static ProxyResponseEvent created() {
		return new ProxyResponseEvent().withStatusCode( 201 );
	}

	static ProxyResponseEvent accepted() {
		return new ProxyResponseEvent().withStatusCode( 202 );
	}

	static ProxyResponseEvent ok(String body) {
		return ok( body, ApiGatewayConfig.INSTANCE.defaultContentType() );
	}

	static ProxyResponseEvent ok(String body, String contentType) {
		val contentTypeHeaders = singletonMap( "Content-Type", contentType );
		return new ProxyResponseEvent().withStatusCode( 200 ).withBody( body ).withHeaders( contentTypeHeaders );
	}

	static ProxyResponseEvent ok(Object body) {
		return ok( body, ApiGatewayConfig.INSTANCE.defaultContentType() );
	}

	static ProxyResponseEvent ok(Object body, String contentType) {
		val serializer = getResponseSerializer(contentType);
		val serialized = serializer.toString( body );
		val contentTypeHeaders = singletonMap( "Content-Type", contentType );
		return new ProxyResponseEvent().withStatusCode( 200 )
				.withBody( serialized.getContent() ).withHeaders( contentTypeHeaders );
	}

	static Serializer getResponseSerializer( String contentType ) {
		if ( contentType == null || contentType.isEmpty() ) {
			contentType = ApiGatewayConfig.INSTANCE.defaultContentType();
			System.out.println( "No content type defined. Using default: " + contentType );
		}

		val serializers = ApiGatewayConfig.INSTANCE.serializers();
		if ( serializers == null )
			throw new RuntimeException( "Could not generate a response: no serializer defined" );

		val serializer = serializers.get( contentType );
		if ( serializer == null )
			throw new RuntimeException( "Could not generate a response: no serializer found for " + contentType );

		return serializer;
	}

	static ProxyResponseEvent with(int statusCode, Map<String, String> headers, String body) {
		return new ProxyResponseEvent()
				.withStatusCode( statusCode )
				.withHeaders( headers )
				.withBody( body );
	}

	static ProxyResponseEvent internalServerError(String errorMessage ) {
		val contentTypeHeaders = singletonMap( "Content-Type", "text/plain" );
		return new ProxyResponseEvent().withStatusCode( 500 ).withBody( errorMessage )
				.withHeaders( contentTypeHeaders );
	}
}
