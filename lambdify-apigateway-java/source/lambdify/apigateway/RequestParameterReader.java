package lambdify.apigateway;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import lombok.val;

/**
 * The reader of {@link APIGatewayProxyRequestEvent} parameters.
 */
public interface RequestParameterReader {

	static <T> T getHeader( APIGatewayProxyRequestEvent request, String key, Class<T> clazz ) {
		val data = request.getHeaders();
		if ( data == null ) return null;
		val value = data.get( key );
		return Config.INSTANCE.paramReader().convert( value, clazz );
	}

	static <T> T getQueryParam( APIGatewayProxyRequestEvent request, String key, Class<T> clazz ) {
		val data = request.getQueryStringParameters();
		if ( data == null ) return null;
		val value = data.get( key );
		return Config.INSTANCE.paramReader().convert( value, clazz );
	}

	static<T> T getPathParam( APIGatewayProxyRequestEvent request, String key, Class<T> clazz ) {
		val data = request.getPathParameters();
		if ( data == null ) return null;
		val value = data.get( key );
		if ( value == null ) return null;
		return Config.INSTANCE.paramReader().convert( value, clazz );
	}

	static<T> T getStageParam( APIGatewayProxyRequestEvent request, String key, Class<T> clazz ) {
		val data = request.getStageVariables();
		if ( data == null ) return null;
		val value = data.get( key );
		return Config.INSTANCE.paramReader().convert( value, clazz );
	}

	static<T> T getBodyAs( APIGatewayProxyRequestEvent request, Class<T> type ) {
		val serializer = getSerializer( request );
		if ( serializer == null )
			throw new RuntimeException( "Could not found a valid serializer for this request." );
		return serializer.toObject( request.getBody(), type, false );
	}

	static String getContentType( APIGatewayProxyRequestEvent request ){
		val contentType = request.getHeaders();
		return contentType == null ? null : contentType.get( "content-type" );
	}

	static Serializer getSerializer( APIGatewayProxyRequestEvent request ) {
		val requestContentType = getContentType( request );
		if ( requestContentType != null ) {
			return Config.INSTANCE.serializers().get( requestContentType );
		}
		return null;
	}
}
