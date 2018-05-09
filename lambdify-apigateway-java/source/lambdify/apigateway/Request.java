package lambdify.apigateway;

import java.util.*;
import lombok.*;

/**
 * Represents an API Gateway request.
 */
@Data
public class Request
{
	String resource;
	String path;
	String httpMethod;
	Map<String, String> headers;
	Map<String, String> queryStringParameters;
	Map<String, String> pathParameters;
	Map<String, String> stageVariables;
	RequestContext requestContext;
	String body;
	boolean isBase64Encoded;

	@Setter transient Serializer serializer;

	public String getContentType(){
		return getHeaders().get( "content-type" );
	}

	public Map<String, String> getHeaders(){
		if ( headers == null )
			headers = new HashMap<>();
		return headers;
	}

	public <T> T getHeader( String key, Class<T> clazz ) {
		val value = getHeaders().get( key );
		return Config.INSTANCE.defaultParamReader().convert( value, clazz );
	}

	public Map<String, String> getQueryStringsParameters(){
		if ( queryStringParameters == null )
			queryStringParameters = new HashMap<>();
		return queryStringParameters;
	}

	public <T> T getQueryParam( String key, Class<T> clazz ) {
		val value = getQueryStringParameters().get( key );
		return Config.INSTANCE.defaultParamReader().convert( value, clazz );
	}

	public Map<String, String> getPathParameters(){
		if ( pathParameters == null )
			pathParameters = new HashMap<>();
		return pathParameters;
	}

	public <T> T getPathParam( String key, Class<T> clazz ) {
		val value = getPathParameters().get( key );
		return Config.INSTANCE.defaultParamReader().convert( value, clazz );
	}

	public Map<String, String> getStageVariables(){
		if ( stageVariables == null )
			stageVariables = new HashMap<>();
		return stageVariables;
	}

	public <T> T getStageParam( String key, Class<T> clazz ) {
		val value = getStageVariables().get( key );
		return Config.INSTANCE.defaultParamReader().convert( value, clazz );
	}

	public <T> T getBodyAs( Class<T> type ) {
		if ( serializer == null )
			throw new RuntimeException( "Could not found a valid serializer for this request." );
		return serializer.toObject( getBody(), type, isBase64Encoded );
	}

	@Data
	public static class RequestContext {
		String apiId;
		String accountId;
		Map<String, String> authorizer;
		Map<String, String> error;
		RequestContextIdentity identity;
		String path;
		String resourceId;
		String stage;
		String requestId;
		String resourcePath;
		String httpMethod;
	}

	@Data
	public static class RequestContextIdentity {
		String accountId;
		String apiKey;
		String apiKeyId;
		String caller;
		String cognitoIdentityPoolId;
		String cognitoIdentityId;
		String cognitoAuthenticationType;
		String cognitoAuthenticationProvider;
		String sourceIp;
		String userArn;
		String userAgent;
		String user;
	}
}


