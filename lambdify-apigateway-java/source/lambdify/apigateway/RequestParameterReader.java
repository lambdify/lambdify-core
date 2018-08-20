package lambdify.apigateway;

import lambdify.aws.events.apigateway.ProxyRequestEvent;
import lombok.val;

/**
 * The reader of {@link ProxyRequestEvent} parameters.
 */
public interface RequestParameterReader {

	static <T> T getHeaderParam(ProxyRequestEvent request, String key, Class<T> clazz ) {
		val data = request.getHeaders();
		if ( data == null ) return null;
		val value = data.get( key );
		return ApiGatewayConfig.INSTANCE.paramReader().convert( value, clazz );
	}

	static <T> T getQueryParam( ProxyRequestEvent request, String key, Class<T> clazz ) {
		val data = request.getQueryStringParameters();
		if ( data == null ) return null;
		val value = data.get( key );
		return ApiGatewayConfig.INSTANCE.paramReader().convert( value, clazz );
	}

	static<T> T getPathParam( ProxyRequestEvent request, String key, Class<T> clazz ) {
		val data = request.getPathParameters();
		if ( data == null ) return null;
		val value = data.get( key );
		if ( value == null ) return null;
		return ApiGatewayConfig.INSTANCE.paramReader().convert( value, clazz );
	}

	static<T> T getStageParam( ProxyRequestEvent request, String key, Class<T> clazz ) {
		val data = request.getStageVariables();
		if ( data == null ) return null;
		val value = data.get( key );
		return ApiGatewayConfig.INSTANCE.paramReader().convert( value, clazz );
	}

	@SuppressWarnings( "unchecked" )
	static<T> T getBodyAs( ProxyRequestEvent request, Class<T> type ) {
		if ( String.class.equals( type ) )
			return (T) request.getBody();
		else if ( byte[].class.equals( type ) )
			return (T) request.getBody().getBytes();

		val serializer = getSerializer( request );
		if ( serializer == null )
			throw new RuntimeException( "Could not found a valid serializer for this request." );
		return serializer.toObject( request.getBody(), type, false );
	}

	static String getContentType( ProxyRequestEvent request ){
		val contentType = request.getHeaders();
		return contentType == null ? null : contentType.get( "content-type" );
	}

	static Serializer getSerializer( ProxyRequestEvent request ) {
		val requestContentType = getContentType( request );
		if ( requestContentType != null ) {
			return ApiGatewayConfig.INSTANCE.serializers().get( requestContentType );
		}
		return null;
	}
}
