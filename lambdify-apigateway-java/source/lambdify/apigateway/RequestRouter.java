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
	final Router.LambdaFunction notFound;

	public RequestRouter(Router.LambdaFunction notFound, Iterable<Serializer> serializers) {
		this.notFound = notFound;
		this.registeredSerializers = new HashMap<>();
		for ( val serializer : serializers ) {
			val previous = registeredSerializers.put( serializer.contentType(), serializer );
			if ( previous != null )
				System.err.println( "Overriding previously registered serializer for " + serializer.contentType() );
		}
	}

	public Response doRouting(Request req, Context ctx) {
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

	private Response serialize(Response response) {
		val serializer = getResponseSerializer( response );
		if ( serializer == null )
			return Response.internalServerError( "Could not generate a response: no serializer found for " + response.contentType );

		val stringified = serializer.toString( response.unserializedBody );
		response.setBody( stringified.getContent() );
		response.setBase64Encoded( stringified.isBase64Encoded() );
		return response;
	}

	private Serializer getResponseSerializer(Response response) {
		var contentType = response.contentType;
		if ( contentType == null ) {
			contentType = Config.INSTANCE.defaultContentType;
			System.out.println( "No content type defined. Using default: " + contentType );
		}
		return registeredSerializers.get( contentType );
	}

	public Router.LambdaFunction resolveRoute(Request req) {
		val found = matchers.computeIfAbsent( req.httpMethod, m -> new ArrayList<>() );
		val urlTokens = URLMatcher.tokenize( req.path );
		var route = notFound;
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
				.add( new Entry( matcher, endpoint.value() ) );
	}


}