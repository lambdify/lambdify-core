package lambdify.apigateway.kotlin

import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import lambdify.apigateway.App
import lambdify.apigateway.Methods
import lambdify.apigateway.Router

/**
 * Created by miere.teixeira on 06/04/2018.
 */
open class App(builder: App.() -> Unit)
    :RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> by App().apply(builder)

infix fun Methods.and(s:String ):Router.Route {
    return and(s);
}

infix fun Router.Route.with( t: (APIGatewayProxyRequestEvent) -> APIGatewayProxyResponseEvent): Router.Entry<Router.Route, Router.LambdaFunction> {
    return with( t );
}

infix fun Router.Route.with( t: () -> APIGatewayProxyResponseEvent): Router.Entry<Router.Route, Router.LambdaFunction> {
    return with( t );
}

infix fun Router.Route.withNoContent(t: (APIGatewayProxyRequestEvent) -> Unit ): Router.Entry<Router.Route, Router.LambdaFunction> {
    return withNoContent( t );
}
