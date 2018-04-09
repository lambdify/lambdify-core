package lambdify.apigateway

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

/**
 * Created by miere.teixeira on 09/04/2018.
 */
class URLTokenizerTest {

    val notFound = { _: APIGatewayRequest -> APIGatewayResponse.notFound() }
    val urlMatcher = UrlMatcher(notFound)

    @DisplayName("Can tokenize the root path")
    @Test fun test1(){
        val tokens = urlMatcher.tokenize("/")
        assertFalse( tokens.isEmpty() )
        assertEquals( 1, tokens.size )
        assertEquals( "/", tokens[0] )
    }

    @DisplayName( "Can tokenize the a multi-level path" )
    @Test fun test2(){
        val tokens = urlMatcher.tokenize("/multi/level/path")
        assertFalse( tokens.isEmpty() )
        assertEquals( 3, tokens.size )
        assertEquals( "/multi", tokens[0] )
        assertEquals( "/level", tokens[1] )
        assertEquals( "/path", tokens[2] )
    }

    @DisplayName( "Can tokenize the a multi-level path that ends with /" )
    @Test fun test3(){
        val tokens = urlMatcher.tokenize("/multi/level/path")
        assertFalse( tokens.isEmpty() )
        assertEquals( 3, tokens.size )
        assertEquals( "/multi", tokens[0] )
        assertEquals( "/level", tokens[1] )
        assertEquals( "/path", tokens[2] )
    }
}