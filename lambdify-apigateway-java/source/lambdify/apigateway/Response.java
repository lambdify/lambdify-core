package lambdify.apigateway;

import java.util.*;
import lombok.*;

/**
 * Represents an API Gateway response.
 */
@Data
public class Response {

	int statusCode = 200;
	Map<String, String> headers;
	String body;
	boolean isBase64Encoded = false;

	@Getter( AccessLevel.NONE )
	@Setter( AccessLevel.NONE )
	transient Object unserializedBody;

	@Getter( AccessLevel.PACKAGE )
	@Setter( AccessLevel.NONE )
	transient String contentType;

	public Response setContentType(String contentType) {
		if ( headers == null )
			headers = new HashMap<>();
		headers.put( "Content-Type", contentType );
		this.contentType = contentType;
		return this;
	}

	boolean requiresSerialization() {
		return this.unserializedBody != null;
	}

	/**
	 * Defines a serialized body.
	 *
	 * @param body
	 * @return
	 */
	public Response setBody(String body) {
		this.body = body;
		this.unserializedBody = null;
		return this;
	}

	/**
	 * Defines an unserializer body. This object will be serialized right before the
	 * a response is send back to the HTTP Client.
	 *
	 * @param body
	 * @return
	 */
	public Response setBody(Object body) {
		this.body = null;
		this.unserializedBody = body;
		return this;
	}

	public static Response notFound() {
		return new Response().setStatusCode( 404 ).setContentType( Config.INSTANCE.defaultContentType() );
	}

	public static Response noContent() {
		return new Response().setStatusCode( 204 ).setContentType( Config.INSTANCE.defaultContentType() );
	}

	public static Response created() {
		return new Response().setStatusCode( 201 ).setContentType( Config.INSTANCE.defaultContentType() );
	}

	public static Response accepted() {
		return new Response().setStatusCode( 202 ).setContentType( Config.INSTANCE.defaultContentType() );
	}

	public static Response ok() {
		return new Response().setStatusCode( 200 ).setContentType( Config.INSTANCE.defaultContentType() );
	}

	public static Response ok(String body) {
		return ok( body, Config.INSTANCE.defaultContentType() );
	}

	public static Response ok(String body, String contentType) {
		return new Response().setStatusCode( 200 ).setBody( body ).setContentType( contentType );
	}

	public static Response ok(Object body) {
		return ok( body, Config.INSTANCE.defaultContentType() );
	}

	public static Response ok(Object body, String contentType) {
		return new Response().setStatusCode( 200 ).setBody( body ).setContentType( contentType );
	}

	public static Response with(int statusCode, Map<String, String> headers, String body) {
		return new Response()
				.setStatusCode( statusCode )
				.setHeaders( headers )
				.setBody( body );
	}

	public static Response internalServerError(String errorMessage) {
		return new Response().setStatusCode( 500 ).setBody( errorMessage )
				.setContentType( Config.INSTANCE.defaultContentType() );
	}
}