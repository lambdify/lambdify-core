package lambdify.apigateway

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.system.measureTimeMillis

/**
 * Created by miere.teixeira on 11/04/2018.
 */
class AuthorizersTest {

    val json = jacksonObjectMapper()

    val request = """
        {
            "type":"TOKEN",
            "authorizationToken":"AUTH TOKEN",
            "methodArn":"arn:aws:execute-api:sa-east-1:123456:rest/Production/GET/api/users"
        }
    """.trimIndent()

    val grantPolicyDocument = """
        {"Version":"2012-10-17","Statement":[{"Condition":{},"Action":"execute-api:Invoke","Resource":["arn:aws:execute-api:sa-east-1:123456:rest/Production/*/*"],"Effect":"Allow"},{"Condition":{},"Action":"execute-api:Invoke","Resource":[],"Effect":"Deny"}]}
    """

    @Test
    fun `can parse many times`(){
        val totals = mutableListOf<Long>()
        for ( i in 0..1000000) {
            totals.add( measureTimeMillis{
                val token = json.readValue<TokenAuthorizerContext>(request)
                assertNotNull( token )
            })
        }

        println( "Avg: ${totals.average()}ms" )
    }

    @Test
    fun `can parse a json and bind as TokenAuthorizerContext`(){
        val token = json.readValue<TokenAuthorizerContext>(request)
        assertEquals( "TOKEN", token.type )
        assertEquals( "AUTH TOKEN", token.authorizationToken )

        val arn = token.method
        assertNotNull( arn )
        assertEquals( "sa-east-1", arn.region )
        assertEquals( "123456", arn.awsAccountId )
        assertEquals( "rest", arn.restApiId )
        assertEquals( "Production", arn.stage )
        assertEquals( "GET", arn.httpMethod )
        assertEquals( "api/users", arn.resource )
    }

    @Test
    fun `grant access from an Authorization Token Request`(){
        val token = json.readValue<TokenAuthorizerContext>(request)
        val policy = token.grantPermission( "1" )
        assertNotNull( policy )
        // TODO: improve the result verification, ensuring the policy is right
        println( "JSON: ${json.writeValueAsString( policy )}" )

        /*val statement = policy.policyDocument["statement"] as Map<String, Map<String, Any>>
        assertTrue( statement.filter { it.value["Effect"] == "Allow" }.first().resource.isNotEmpty() )
        assertTrue( statement.filter { it.effect == "Deny" }.first().resource.isEmpty() )*/
    }

    @Test
    fun `deny access from an Authorization Token Request`(){
        val token = json.readValue<TokenAuthorizerContext>(request)
        val policy = token.denyPermission( "1" )
        assertNotNull( policy )
        // TODO: improve the result verification, ensuring the policy is right
        println( "JSON: ${json.writeValueAsString( policy )}" )

        /*val statement = policy.policyDocumentObject.statement
        assertTrue( statement.filter { it.effect == "Deny" }.first().resource.isNotEmpty() )
        assertTrue( statement.filter { it.effect == "Allow" }.first().resource.isEmpty() )*/
    }
}