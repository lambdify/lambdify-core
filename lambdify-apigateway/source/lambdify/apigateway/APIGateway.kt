package lambdify.apigateway

/**
 * Created by miere.teixeira on 06/04/2018.
 */
class APIGatewayRequest {
    var resource: String? = null
    var path: String = ""
    var httpMethod: String? = null
    var headers: Map<String, String>? = null
    var queryStringParameters: Map<String, String>? = null
    var pathParameters: Map<String, String>? = null
    var stageVariables: Map<String, String>? = null
    var requestContext: APIGatewayRequestContext? = null
    var body: String? = null
    var isBase64Encoded: Boolean = false
}

class APIGatewayRequestContext {
    var accountId: String? = null
    var resourceId: String? = null
    var stage: String? = null
    var requestId: String? = null
    var identity: APIGatewayRequestContextIdentity? = null
    var resourcePath: String? = null
    var httpMethod: String? = null
    var apiId: String? = null
}

class APIGatewayRequestContextIdentity {
    var cognitoIdentityPoolId: String? = null
    var accountId: String? = null
    var cognitoIdentityId: String? = null
    var caller: String? = null
    var apiKey: String? = null
    var sourceIp: String? = null
    var cognitoAuthenticationType: String? = null
    var cognitoAuthenticationProvider: String? = null
    var userArn: String? = null
    var userAgent: String? = null
    var user: String? = null
}

class APIGatewayResponse {
    var statusCode: Int = 0
    var headers: Map<String, String>? = null
    var body: String? = null

    companion object {

        private val DEFAULT_CONTENT_TYPE = hashMapOf( "Content-Type" to "text/plain" )

        fun ok(): APIGatewayResponse = withStatus(200)

        fun ok(newBody: String,
               headers: Map<String, String> = DEFAULT_CONTENT_TYPE): APIGatewayResponse {
            return APIGatewayResponse().apply {
                this.statusCode = 200
                this.headers = headers
                this.body = newBody
            }
        }

        fun noContent(): APIGatewayResponse = withStatus(204)

        fun notFound(): APIGatewayResponse = withStatus(404)

        fun notAuthenticated(): APIGatewayResponse = withStatus(401)

        fun created(): APIGatewayResponse = withStatus(201)

        fun withStatus(statusCode: Int): APIGatewayResponse = with(statusCode, emptyMap())

        fun with(newStatusCode: Int, newHeaders: Map<String, String>): APIGatewayResponse =
            APIGatewayResponse().apply {
                statusCode = newStatusCode
                headers = newHeaders
            }
    }
}

