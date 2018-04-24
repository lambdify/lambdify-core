package lambdify.apigateway.kotlin

import com.amazonaws.services.lambda.runtime.RequestHandler
import lambdify.apigateway.APIGateway.Request
import lambdify.apigateway.APIGateway.Response
import lambdify.apigateway.App
import lambdify.apigateway.Methods
import lambdify.apigateway.Router

/**
 * Created by miere.teixeira on 06/04/2018.
 */
open class App(builder: App.() -> Unit)
    :RequestHandler<Request, Response> by App().apply(builder)

infix fun Methods.and(s:String ):Router.Route {
    return and(s);
}

infix fun Router.Route.with( t: (Request) -> Response): Router.Entry<Router.Route, Router.LambdaFunction> {
    return with( t );
}

infix fun Router.Route.with( t: () -> Response): Router.Entry<Router.Route, Router.LambdaFunction> {
    return with( t );
}

infix fun Router.Route.withNoContent(t: (Request) -> Unit ): Router.Entry<Router.Route, Router.LambdaFunction> {
    return withNoContent( t );
}
