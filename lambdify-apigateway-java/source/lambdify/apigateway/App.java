package lambdify.apigateway;

import java.io.*;
import com.amazonaws.services.lambda.runtime.*;
import lambdify.aws.events.apigateway.*;
import lambdify.core.LambdaStreamFunction;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * A simple AWS Lambda application that handles API Gateway requests.
 */
@ToString
@NoArgsConstructor
@Accessors(fluent = true)
public class App extends LambdaStreamFunction<ProxyRequestEvent, ProxyResponseEvent> {

    /**
     * The internal router.
     */
    private RequestRouter router;

    /**
     * Lazy loader of the internal {@link RequestRouter}.
     *
     * @return
     */
    private RequestRouter getRouter(){
        if ( router == null )
            router = new RequestRouter();
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
    public final App routes(Router.Entry<Router.Route, Router.LambdaFunction>... routes) {
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
    public final App routers( Router...routers ) {
        for ( val router : routers )
            routes( router.getRoutes() );
        return this;
    }

    /**
     * Handles a Lambda Function request.
     *
     * @param request The Lambda Function input
     * @param context The Lambda execution environment context object.
     * @return The Lambda Function output
     *
     * @see RequestHandler#handleRequest(Object, Context)
     */
    @Override
    public ProxyResponseEvent handleRequest(ProxyRequestEvent request, Context context) {
        try {
            return getRouter().doRouting( request, context );
        } catch ( Throwable cause ) {
            val error = new StringWriter();
            cause.printStackTrace( new PrintWriter( error ) );
            val errorMsg = error.toString();
            context.getLogger().log( "Failed to handle request: " + cause.getMessage() );
            context.getLogger().log( errorMsg );
            context.getLogger().log( "Global configuration: " + ApiGatewayConfig.INSTANCE );
	        context.getLogger().log( "App configuration: " + this );
            return Responses.internalServerError( errorMsg );
        }
    }
}