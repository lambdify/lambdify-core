package lambdify.apigateway;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import lambdify.apigateway.APIGateway.Request;
import lambdify.apigateway.APIGateway.Response;
import lambdify.apigateway.APIGateway.Serializer;
import lombok.Value;
import lombok.experimental.var;
import lombok.val;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static lambdify.apigateway.URL.compile;

/**
 * Created by miere.teixeira on 18/04/2018.
 */
public interface Router {

    @Value class URLRouter {

        final Map<String, List<Entry<URL.URLMatcher, LambdaFunction>>> matchers = new HashMap<>();

        final Map<String, Serializer> registeredSerializers;
        final LambdaFunction notFound;

        public URLRouter( LambdaFunction notFound, Iterable<Serializer> serializers ) {
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
                response = serialize(response);
            return response;
        }

        private Response serialize(Response response) {
            val serializer = registeredSerializers.get( response.contentType );
            if ( serializer == null )
                return Response.internalServerError( "Could not generate a response: no serializer found for " + response.contentType );

            val stringified = serializer.toString( response.unserializedBody );
            response.setBody( stringified.getContent() );
            response.setBase64Encoded( stringified.isBase64Encoded() );
            return response;
        }

        public LambdaFunction resolveRoute(APIGateway.Request req ) {
            val found = matchers.computeIfAbsent( req.httpMethod, m -> new ArrayList<>() );
            val urlTokens = URL.tokenize(req.path);
            var route = notFound;
            for ( val entry : found )  {
                val params = new HashMap<String, String>();
                if ( entry.key.matches( urlTokens, params ) ) {
                    route = entry.value;
                    req.pathParameters = params;
                    break;
                }
            }
            return route;
        }

        public void memorizeEndpoint( Entry<Route, LambdaFunction> endpoint ) {
            val method = endpoint.key.method.toString();
            val matcher = compile( endpoint.key.url );
            matchers
                .computeIfAbsent( method, k -> new ArrayList<>() )
                .add( new Entry(matcher, endpoint.value) );
        }
    }

    class DefaultNotFoundHandler implements LambdaFunction {

        @Override
        public Response invoke(Request input) {
            return Response.notFound();
        }
    }

    @Value class Route {
        final String url;
        final Methods method;

        public Entry<Route, LambdaFunction> with( LambdaSupplier target ) {
            return new Entry<>( this, target );
        }

        public Entry<Route, LambdaFunction> with( LambdaFunction target ) {
            return new Entry<>( this, target );
        }

        public Entry<Route, LambdaFunction> withNoContent( LambdaConsumer target ) {
            return new Entry<>( this, target );
        }
    }

    @Value class Entry<K,V> {
        final K key;
        final V value;
    }

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

    interface LambdaConsumer extends LambdaFunction {

        @Override
        default Response invoke(Request input) {
            consume(input);
            return Response.noContent();
        }

        void consume(Request input);
    }

    interface LambdaSupplier extends LambdaFunction {

        @Override
        default Response invoke(Request input) {
            return supply();
        }

        Response supply();
    }
}
