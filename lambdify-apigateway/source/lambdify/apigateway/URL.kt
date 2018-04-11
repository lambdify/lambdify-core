package lambdify.apigateway

/**
 * Created by miere.teixeira on 06/04/2018.
 */
class UrlMatcher( val notFound: AwsRequestHandlerAsFunction) {

    val matchers:MutableMap<String, MutableList<EndpointMatcher>> = mutableMapOf()

    fun matchRoute( req: APIGatewayRequest): AwsRequestHandlerAsFunction {
        val found = matchers[req.httpMethod]
        val urlTokens = tokenize(req.path)
        var route = notFound
        if ( found != null )
            for ( entry in found )  {
                val params = mutableMapOf<String, String>()
                if ( entry.first.matches( urlTokens, params ) ) {
                    route = entry.second
                    req.pathParameters = params
                    break
                }
            }
        return route
    }

    fun memorizeEndpoint( endpoint: Endpoint) {
        val method = endpoint.first.method.toString()
        val compiled = compile( endpoint.first.url )
        val matcher = Matcher(compiled)
        matchers
            .computeIfAbsent( method, { mutableListOf() })
            .add( Pair(matcher, endpoint.second) )
    }

    private fun compile( url:String ): Compiled {
        return tokenize(url).map {
            if ( it.length > 1 && it[1] == ':' ) PlaceHolder(it.substring(1))
            else Equals(it)
        }
    }

    fun tokenize(path:String ): List<String> {
        return when {
            path == "/" -> listOf( "/" )
            path.isEmpty() -> emptyList()
            else -> {
                val tokens = mutableListOf<String>()
                val url = if (path.last() != '/') path
                else path.substring(0, path.length - 1)
                url.split('/').forEach {
                    if (!it.isEmpty())
                        tokens.add("/$it")
                }
                tokens
            }
        }
    }
}

class Matcher( val compiled: Compiled) {
    fun matches(tokens: List<String>, ctx:MutableMap<String,String>): Boolean {
        if ( tokens.size == compiled.size ) {
            for ((token, entry) in tokens.zip(compiled))
                if (!entry.invoke(token, ctx))
                    return false
            return true
        }
        return false
    }
}

class Equals( val value:String ): CompiledEntry {
    override fun invoke(p1: String, ctx:MutableMap<String,String>): Boolean {
        return value.equals(p1)
    }
}

class PlaceHolder( val key:String ): CompiledEntry {
    override fun invoke( value: String, ctx:MutableMap<String,String> ): Boolean {
        ctx.put( key, value )
        return true
    }
}

enum class Methods {
    GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS, ALL;

    infix fun and(url: String ): Route = Route(this, url)
}

class Route(val method: Methods, val url:String ) {

    infix fun with(target: AwsRequestHandlerAsFunction): Endpoint
            = Pair( this, target )

    infix fun withNoResponse(target: AwsRequestHandlerAsConsumer): Endpoint
            = Pair( this, { req ->
        target( req )
        APIGatewayResponse.noContent()
    })

    infix fun withNoArgs(target: AwsRequestHandlerAsSupplier): Endpoint
            = Pair( this, { _ -> target() })
}
