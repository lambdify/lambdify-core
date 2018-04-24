package lambdify.apigateway.kotlin

import com.amazonaws.services.lambda.runtime.RequestHandler
import lambdify.apigateway.APIGateway
import lambdify.apigateway.App

/**
 * Created by miere.teixeira on 06/04/2018.
 */
open class App(builder: App.() -> Unit)
    :RequestHandler<APIGateway.Request, APIGateway.Response> by App().apply(builder)



