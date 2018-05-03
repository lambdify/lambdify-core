package lambdify.apigateway;

import java.util.ServiceLoader;
import com.amazonaws.services.lambda.runtime.*;
import lambdify.apigateway.APIGateway.*;
import lambdify.apigateway.Router.*;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * A simple AWS Lambda application that handles API Gateway requests.
 */
@NoArgsConstructor
@Accessors(fluent = true)
public class App implements RequestHandler<Request, Response> {

    /**
     * The request serializers. By default it loads all {@code Serializer}s found at the class path.
     */
    @Setter Iterable<APIGateway.Serializer> serializers;

    /**
     * Handles requests which Method and URL does not matches any previously defined route.
     */
    @Setter LambdaFunction notFoundHandler;

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
            router = new Router.URLRouter(getNotFoundHandler(), getSerializers());
        return router;
    }

    /**
     * Lazy loader of {@code notFoundHandler}.
     *
     * @return
     */
    private LambdaFunction getNotFoundHandler(){
        if ( notFoundHandler == null )
            notFoundHandler = new Router.DefaultNotFoundHandler();
        return notFoundHandler;
    }

    /**
     * Lazy loader of {@code serializers}.
     * @return
     */
    private Iterable<Serializer> getSerializers() {
        if ( serializers == null )
            serializers = ServiceLoader.load(APIGateway.Serializer.class);
        return serializers;
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
     * Memorize the routers (and its routes). Due to architecture decisions, once you define
     * a route you can't change how the internal router behaves. If you intent to define your
     * own {@code notFoundHandler} or even a custom set of serializers you should do this
     * before you define your first route.
     *
     * @param routers
     * @return
     */
    @SafeVarargs
    public final App routers( Router... routers ) {
        for ( val router : routers )
            routes( router.getRoutes() );
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