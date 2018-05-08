package lambdify.apigateway;

import com.amazonaws.services.lambda.runtime.RequestHandler;

/**
 * Represents an Authorizer Function.
 */
interface AuthorizerFunction extends RequestHandler<TokenAuthorizerContext, AuthPolicy> {

}