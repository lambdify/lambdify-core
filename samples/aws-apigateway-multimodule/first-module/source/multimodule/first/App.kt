package multimodule.first

import lambdify.apigateway.APIGateway
import lambdify.apigateway.Methods
import lambdify.apigateway.kotlin.App
import lambdify.apigateway.kotlin.*

class App:App({

    routes(
        Methods.GET and "/first" with ::sayFirst,
        Methods.GET and "/first/second" with ::sayFirstAndSecond
    )
})

fun sayFirst() = APIGateway.Response.ok( "First" )

fun sayFirstAndSecond() = APIGateway.Response.accepted().apply {
    headers = mapOf( "Location" to "/first/" )
}
