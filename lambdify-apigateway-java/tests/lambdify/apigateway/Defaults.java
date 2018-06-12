package lambdify.apigateway;

import java.util.Collections;
import lambdify.aws.events.apigateway.ProxyRequestEvent;

/**
 *
 */
public interface Defaults {

	static ProxyRequestEvent createRequest(String path, Methods method ) {
		return new ProxyRequestEvent()
				.withPath( path )
				.withHttpMethod( method.toString() )
				.withHeaders( Collections.emptyMap() );
	}
}
