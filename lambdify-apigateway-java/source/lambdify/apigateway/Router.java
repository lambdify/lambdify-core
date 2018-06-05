package lambdify.apigateway;

import com.amazonaws.services.lambda.runtime.*;
import lambdify.aws.events.apigateway.*;
import lambdify.core.LambdaStreamFunction;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * Defines how a router should behave inside an AWS Lambda application.
 *
 * Created by miere.teixeira on 18/04/2018.
 */
public interface Router {

    Entry<Route, LambdaFunction>[] getRoutes();

    /**
     * Defines a Route.
     */
    @Value @Accessors(fluent = true)
    class Route {

        final String url;
        final Methods method;

        public Entry<Route, LambdaFunction> with(LambdaSupplier target ) {
            return new Entry<>( this, target );
        }

        public Entry<Route, LambdaFunction> with( LambdaFunction target ) {
            return new Entry<>( this, target );
        }

        public Entry<Route, LambdaFunction> withNoContent( LambdaConsumer target ) {
            return new Entry<>( this, target );
        }
    }

	/**
	 * Represents a Lambda Function.
	 */
	interface LambdaFunction extends RequestHandler<ProxyRequestEvent, ProxyResponseEvent> {

		@Override
		default ProxyResponseEvent handleRequest(ProxyRequestEvent input, Context context) {
			return invoke(input);
		}

		ProxyResponseEvent invoke(ProxyRequestEvent input);
	}

	/**
	 * Represents a Lambda Function that does not produce custom response.
	 */
	interface LambdaConsumer extends LambdaFunction {

	    @Override
	    default ProxyResponseEvent invoke(ProxyRequestEvent input) {
	        consume(input);
	        return Responses.noContent();
	    }

	    void consume(ProxyRequestEvent input);
	}

	/**
	 * Represents a Lambda Function that only produce custom responses.
	 */
	interface LambdaSupplier extends LambdaFunction {

	    @Override
	    default ProxyResponseEvent invoke(ProxyRequestEvent input) {
	        return supply();
	    }

		ProxyResponseEvent supply();
	}

	/**
	 * Represents an Authorizer Function.
	 */
	abstract class AuthorizerFunction extends LambdaStreamFunction<TokenAuthorizerContext, AuthPolicy> {

	}

	/**
	 * A simple holder for Key and Value.
	 *
	 * @param <K>
	 * @param <V>
	 */
	@Value @Accessors(fluent = true)
	class Entry<K,V> {
		final K key;
		final V value;
	}
}
