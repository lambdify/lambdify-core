package multimodule.second

import lambdify.apigateway.APIGateway
import lambdify.apigateway.Methods
import lambdify.apigateway.kotlin.App
import lambdify.apigateway.kotlin.*

class App:App({

    routes(
        Methods.GET and "/second" with ::saySecond
    )
})

fun saySecond() = APIGateway.Response.ok( "Second" )
