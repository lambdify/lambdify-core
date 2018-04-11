package lambdify.apigateway

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler

/**
 * Created by miere.teixeira on 06/04/2018.
 */
open class APIGatewayApp(builder: RouteBuilder.() -> Unit) : RequestHandler<APIGatewayRequest, APIGatewayResponse> {

    private val matcher = RouteBuilder().apply(builder).matcher

    override fun handleRequest(input: APIGatewayRequest, context: Context?): APIGatewayResponse {
        val matched = matcher.matchRoute( input )
        return matched.invoke(input)
    }
}

open class RouteBuilder {

    var notFoundHandler: AwsRequestHandlerAsFunction = { APIGatewayResponse.notFound() }
    val matcher by lazy { UrlMatcher(notFoundHandler) }

    fun routes(vararg route: Endpoint) {
        route.forEach { matcher.memorizeEndpoint( it ) }
    }
}



