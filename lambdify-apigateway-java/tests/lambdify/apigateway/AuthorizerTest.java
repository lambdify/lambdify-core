package lambdify.apigateway;

import static java.lang.System.out;
import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import com.fasterxml.jackson.jr.ob.JSON;
import lombok.val;
import org.junit.jupiter.api.*;

class AuthorizerTest {
    
    String
        request = "{\n" +
            "    \"type\":\"TOKEN\",\n" +
            "    \"authorizationToken\":\"AUTH TOKEN\",\n" +
            "    \"methodArn\":\"arn:aws:execute-api:sa-east-1:123456:rest/Production/GET/api/users\"\n" +
            "}",

        grantPolicyDocumento = "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Condition\":{},\"Action\":\"execute-api:Invoke\",\"Resource\":[\"arn:aws:execute-api:sa-east-1:123456:rest/Production/*/*\"],\"Effect\":\"Allow\"},{\"Condition\":{},\"Action\":\"execute-api:Invoke\",\"Resource\":[],\"Effect\":\"Deny\"}]}"
    ;

    @DisplayName("can parse a json and bind as TokenAuthorizerContext")
    @Test void test1() throws IOException {
        val token = JSON.std.beanFrom(TokenAuthorizerContext.class, request);
        assertEquals( "TOKEN", token.type );
        assertEquals( "AUTH TOKEN", token.authorizationToken );

        val arn = token.getMethod();
        assertNotNull( arn );
        assertEquals( "sa-east-1", arn.getRegion());
        assertEquals( "123456", arn.getAwsAccountId());
        assertEquals( "rest", arn.getRestApiId());
        assertEquals( "Production", arn.getStage());
        assertEquals( "GET", arn.getHttpMethod());
        assertEquals( "api/users", arn.getResource());
    }

    @DisplayName("grant access from an Authorization Token APIGatewayProxyRequestBlobEvent")
    @Test void test2() throws IOException {
        val token = JSON.std.beanFrom(TokenAuthorizerContext.class, request);
        val policy = token.grantPermission( "1" );
        assertNotNull( policy );
        // TODO: improve the result verification, ensuring the policy is right
        out.println( "JSON: " + JSON.std.asString(policy) );
    }

    @DisplayName("grant access from an Authorization Token APIGatewayProxyRequestBlobEvent")
    @Test void test3() throws IOException {
        val token = JSON.std.beanFrom(TokenAuthorizerContext.class, request);
        val policy = token.denyPermission( "1" );
        assertNotNull( policy );
        // TODO: improve the result verification, ensuring the policy is right
        out.println( "JSON: " + JSON.std.asString(policy) );
    }
}
