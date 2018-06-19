package lambdify.apigateway.apt;

import java.util.Map;
import lambdify.apigateway.ann.Context;
import lambdify.aws.events.apigateway.ProxyRequestEvent;

/**
 *
 */
class Account {

	final String token;

	private Account(String token) {
		this.token = token;
	}

	@Context
	static Account produceAccount(ProxyRequestEvent requestEvent) {
		final Map<String, String> authorizer = requestEvent.getRequestContext().getAuthorizer();
		return new Account( authorizer.get( "principalId" ) );
	}
}
