package lambdify.apigateway;

import static lambdify.apigateway.URLMatcher.compile;
import java.util.*;
import com.amazonaws.services.lambda.runtime.Context;
import lambdify.apigateway.Router.*;
import lombok.*;
import lombok.experimental.var;

/**
 * Seeks for {@code LambdaFunction}s that matches an specific URL and Http Method.
 */
@Value
public class RequestRouter {

	final Map<String, List<Entry<URLMatcher, Router.LambdaFunction>>> matchers = new HashMap<>();

	final Map<String, Serializer> registeredSerializers;

	public RequestRouter(Iterable<Serializer> serializers) {
		this.registeredSerializers = new HashMap<>();
		for ( val serializer : serializers ) {
			val previous = registeredSerializers.put( serializer.contentType(), serializer );
			if ( previous != null )
				System.err.println( "Overriding previously registered serializer for " + serializer.contentType() );
		}
	}

	public Response doRouting(Request req, Context ctx) {
		normalizeHeaders( req );
		val requestContentType = req.getContentType();
		if ( requestContentType != null ) {
			val serializer = registeredSerializers.get( requestContentType );
			req.setSerializer( serializer );
		}

		val route = resolveRoute( req );

		var response = route.handleRequest( req, ctx );
		if ( response.requiresSerialization() )
			response = serialize( response );
		return response;
	}

	private void normalizeHeaders(Request req) {
		val newHeaders = new HashMap<String, String>();
		for ( val entry : req.getHeaders().entrySet() )
			newHeaders.put( entry.getKey().toLowerCase(), entry.getValue() );
		req.setHeaders( newHeaders );
	}

	private Response serialize(Response response) {
		val serializer = getResponseSerializer( response );
		val stringified = serializer.toString( response.unserializedBody );
		response.setBody( stringified.getContent() );
		response.setBase64Encoded( stringified.isBase64Encoded() );
		return response;
	}

	private Serializer getResponseSerializer(Response response) {
		var contentType = response.contentType;
		if ( contentType == null || contentType.isEmpty() ) {
			contentType = Config.INSTANCE.defaultContentType();
			System.out.println( "No content type defined. Using default: " + contentType );
		}
		val serializer = registeredSerializers.get( contentType );
		if ( serializer == null )
			throw new RuntimeException( "Could not generate a response: no serializer found for " + contentType );
		return serializer;
	}

	public Router.LambdaFunction resolveRoute(Request req) {
		val found = matchers.computeIfAbsent( req.httpMethod, m -> new ArrayList<>() );
		val urlTokens = URLMatcher.tokenize( req.path );
		var route = Config.INSTANCE.defaultNotFoundHandler();
		for ( val entry : found ) {
			val params = new HashMap<String, String>();
			if ( entry.key().matches( urlTokens, params ) ) {
				route = entry.value();
				req.pathParameters = params;
				break;
			}
		}
		return route;
	}

	public void memorizeEndpoint(Entry<Route, Router.LambdaFunction> endpoint) {
		val method = endpoint.key().method().toString();
		val matcher = compile( endpoint.key().url() );
		matchers
				.computeIfAbsent( method, k -> new ArrayList<>() )
				.add( new Entry<>( matcher, endpoint.value() ) );
	}


}