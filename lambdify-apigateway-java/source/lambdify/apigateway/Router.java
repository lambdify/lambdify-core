package lambdify.apigateway;

import java.io.*;
import com.amazonaws.services.lambda.runtime.*;
import lombok.*;
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
	interface LambdaFunction extends RequestHandler<Request, Response> {

		@Override
		default Response handleRequest(Request input, Context context) {
			try {
				return invoke(input);
			} catch ( Throwable cause ) {
				val writer = new StringWriter();
				cause.printStackTrace( new PrintWriter(writer));
				return Response.internalServerError( writer.toString() );
			}
		}

		Response invoke(Request input);
	}

	/**
	 * Represents a Lambda Function that does not produce custom response.
	 */
	interface LambdaConsumer extends LambdaFunction {

	    @Override
	    default Response invoke(Request input) {
	        consume(input);
	        return Response.noContent();
	    }

	    void consume(Request input);
	}

	/**
	 * Represents a Lambda Function that only produce custom responses.
	 */
	interface LambdaSupplier extends LambdaFunction {

	    @Override
	    default Response invoke(Request input) {
	        return supply();
	    }

	    Response supply();
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
