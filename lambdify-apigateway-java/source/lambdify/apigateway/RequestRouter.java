package lambdify.apigateway;

import static lambdify.apigateway.URLMatcher.compile;
import java.util.*;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.*;
import lambdify.apigateway.Router.*;
import lombok.*;
import lombok.experimental.var;

/**
 * Seeks for {@link Router.LambdaFunction}s that matches an specific URL and Http Method.
 */
@Value
public class RequestRouter {

	final Map<String, List<Entry<URLMatcher, Router.LambdaFunction>>> matchers = new HashMap<>();

	public APIGatewayProxyResponseEvent doRouting(APIGatewayProxyRequestEvent req, Context ctx) {
		normalizeHeaders( req );
		val route = resolveRoute( req );
		return route.handleRequest( req, ctx );
	}

	private void normalizeHeaders(APIGatewayProxyRequestEvent req) {
		val newHeaders = new HashMap<String, String>();
		for ( val entry : req.getHeaders().entrySet() )
			newHeaders.put( entry.getKey().toLowerCase(), entry.getValue() );
		req.setHeaders( newHeaders );
	}

	Router.LambdaFunction resolveRoute(APIGatewayProxyRequestEvent req) {
		val found = matchers.computeIfAbsent( req.getHttpMethod(), m -> new ArrayList<>() );
		val urlTokens = URLMatcher.tokenize( req.getPath() );
		var route = Config.INSTANCE.defaultNotFoundHandler();
		for ( val entry : found ) {
			val params = new HashMap<String, String>();
			if ( entry.key().matches( urlTokens, params ) ) {
				route = entry.value();
				req.setPathParameters( params );
				break;
			}
		}
		return route;
	}

	void memorizeEndpoint(Entry<Route, Router.LambdaFunction> endpoint) {
		val method = endpoint.key().method().toString();
		val matcher = compile( endpoint.key().url() );
		matchers
			.computeIfAbsent( method, k -> new ArrayList<>() )
			.add( new Entry<>( matcher, endpoint.value() ) );
	}


}