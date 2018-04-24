package lambdify.apigateway;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class URLTokenizerTest {


    @DisplayName("Can tokenize the root path")
    @Test void test1(){
        val tokens = URL.tokenize("/");
        assertFalse( tokens.isEmpty() );
        assertEquals( 1, tokens.size() );
        assertEquals( "/", tokens.get(0) );
    }

    @DisplayName( "Can tokenize the a multi-level path" )
    @Test void test2(){
        val tokens = URL.tokenize("/multi/level/path");
        assertFalse( tokens.isEmpty() );
        assertEquals( 3, tokens.size() );
        assertEquals( "/multi", tokens.get(0) );
        assertEquals( "/level", tokens.get(1) );
        assertEquals( "/path", tokens.get(2) );
    }

    @DisplayName( "Can tokenize the a multi-level path that ends with /" )
    @Test
    void test3(){
        val tokens = URL.tokenize("/multi/level/path");
        assertFalse( tokens.isEmpty() );
        assertEquals( 3, tokens.size() );
        assertEquals( "/multi", tokens.get(0) );
        assertEquals( "/level", tokens.get(1) );
        assertEquals( "/path", tokens.get(2) );
    }
    
}
