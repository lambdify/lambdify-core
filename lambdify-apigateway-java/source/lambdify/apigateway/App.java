package lambdify.apigateway;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import lambdify.apigateway.APIGateway.Request;
import lambdify.apigateway.APIGateway.Response;
import lambdify.apigateway.Router.Entry;
import lambdify.apigateway.Router.LambdaFunction;
import lambdify.apigateway.Router.Route;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.val;

import java.util.ServiceLoader;

/**
 * A simple AWS Lambda application that handles API Gateway requests.
 */
@NoArgsConstructor
@Accessors(fluent = true)
public class App implements RequestHandler<Request, Response> {

    /**
     * The request serializers. By default it loads all {@code Serializer}s found at the class path.
     */
    @Setter Iterable<APIGateway.Serializer> serializers = ServiceLoader.load(APIGateway.Serializer.class);

    /**
     * Handles requests which Method and URL does not matches any previously defined route.
     */
    @Setter LambdaFunction notFoundHandler = new Router.DefaultNotFoundHandler();

    /**
     * The internal router.
     */
    private Router.URLRouter router;

    /**
     * Lazy loader of the internal {@link Router.URLRouter}.
     * @return
     */
    private Router.URLRouter getRouter(){
        if ( router == null )
            router = new Router.URLRouter(notFoundHandler, serializers);
        return router;
    }

    /**
     * Define routes for your application. Due to architecture decisions, once you define
     * a route you can't change how the internal router behaves. If you intent to define your
     * own {@code notFoundHandler} or even a custom set of serializers you should do this
     * before you define your first route.
     *
     * @param routes
     * @return
     */
    @SafeVarargs
    public final App routes(Entry<Route, LambdaFunction>... routes) {
        for ( val route : routes )
            getRouter().memorizeEndpoint( route );
        return this;
    }

    /**
     * Handles a Lambda Function request
     * @param request The Lambda Function input
     * @param context The Lambda execution environment context object.
     * @return The Lambda Function output
     *
     * @see RequestHandler#handleRequest(Object, Context)
     */
    @Override
    public Response handleRequest(Request request, Context context) {
        return getRouter().doRouting( request, context );
    }
}