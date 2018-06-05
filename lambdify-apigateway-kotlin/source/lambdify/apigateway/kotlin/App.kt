package lambdify.apigateway.kotlin

import com.amazonaws.services.lambda.runtime.RequestStreamHandler
import lambdify.apigateway.App
import lambdify.apigateway.Methods
import lambdify.apigateway.Router
import lambdify.aws.events.apigateway.ProxyRequestEvent
import lambdify.aws.events.apigateway.ProxyResponseEvent

/**
 * Created by miere.teixeira on 06/04/2018.
 */
open class App(val builder: App.() -> Unit)
    : RequestStreamHandler by App().apply(builder)

infix fun Methods.and(s:String ):Router.Route {
    return and(s)
}

infix fun Router.Route.with( t: (ProxyRequestEvent) -> ProxyResponseEvent): Router.Entry<Router.Route, Router.LambdaFunction> {
    return with( t )
}

infix fun Router.Route.with( t: () -> ProxyResponseEvent): Router.Entry<Router.Route, Router.LambdaFunction> {
    return with( t )
}

infix fun Router.Route.withNoContent(t: (ProxyRequestEvent) -> Unit ): Router.Entry<Router.Route, Router.LambdaFunction> {
    return withNoContent( t )
}
