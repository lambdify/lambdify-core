package lambdify.apigateway;

import static lambdify.apigateway.URLMatcher.compile;
import java.util.*;
import com.amazonaws.services.lambda.runtime.Context;
import lambdify.apigateway.Router.*;
import lambdify.aws.events.apigateway.*;
import lombok.*;
import lombok.experimental.var;

/**
 * Seeks for {@link Router.LambdaFunction}s that matches an specific URL and Http Method.
 */
@Value
public class RequestRouter {

	final Map<String, List<Entry<URLMatcher, Router.LambdaFunction>>> matchers = new HashMap<>();

	public ProxyResponseEvent doRouting(ProxyRequestEvent req, Context ctx) {
		normalizeHeaders( req );
		val route = resolveRoute( req );
		return route.handleRequest( req, ctx );
	}

	private void normalizeHeaders(ProxyRequestEvent req) {
		val newHeaders = new HashMap<String, String>();
		val headers = req.getHeaders();
		if ( headers != null )
			for ( val entry : headers.entrySet() )
				newHeaders.put( entry.getKey().toLowerCase(), entry.getValue() );
		req.setHeaders( newHeaders );
	}

	Router.LambdaFunction resolveRoute(ProxyRequestEvent req) {
		val found = matchers.computeIfAbsent( req.getHttpMethod(), m -> new ArrayList<>() );
		val urlTokens = URLMatcher.tokenize( req.getPath() );
		var route = ApiGatewayConfig.INSTANCE.defaultNotFoundHandler();
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