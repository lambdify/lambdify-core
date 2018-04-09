package lambdify.apigateway

/**
 * Created by miere.teixeira on 06/04/2018.
 */

typealias Endpoint = Pair<Route, AwsRequestHandlerAsFunction>
typealias Compiled = List<CompiledEntry>
typealias CompiledEntry = Function2<String, MutableMap<String,String>, Boolean>
typealias EndpointMatcher = Pair<Matcher, AwsRequestHandlerAsFunction>

typealias AwsRequestHandlerAsFunction = (APIGatewayRequest) -> APIGatewayResponse
typealias AwsRequestHandlerAsConsumer = (APIGatewayRequest) -> Unit
typealias AwsRequestHandlerAsSupplier = () -> APIGatewayResponse