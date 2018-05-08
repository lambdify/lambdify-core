package lambdify.apigateway;

import java.util.*;
import lombok.*;
import lombok.experimental.var;

@Data
public class TokenAuthorizerContext {

    String type;
    String authorizationToken;
    String methodArn;
    String httpMethod;

    Map<String, String> headers;
    Map<String, String> queryStringParameters;
    Map<String, String> pathParameters;
    Map<String, String> stageVariables;
    Map<String, Object> requestContext;

    public MethodArn getMethod(){
        val arnPartials = methodArn.split(":");
        val region = arnPartials[3];
        val awsAccountId = arnPartials[4];
        val apiGatewayArnPartials = arnPartials[5].split("/");
        val restApiId = apiGatewayArnPartials[0];
        val stage = apiGatewayArnPartials[1];
        val httpMethod = apiGatewayArnPartials[2];
        var resource = "";
        if (apiGatewayArnPartials.length > 3 ) {
            val tokens = Arrays.asList(Arrays.copyOfRange(apiGatewayArnPartials, 3, apiGatewayArnPartials.length));
            resource = String.join( "/", tokens );
        }
        return new MethodArn(region, awsAccountId, restApiId, stage, httpMethod, resource);
    }

    public AuthPolicy grantPermission(String principalId) {
        return grantPermission(principalId, null, null);
    }

    public AuthPolicy grantPermission(String principalId, Map<String, String> context, String usageIdentifierKey ) {
        val method = getMethod();
        val policy = PolicyDocument.allowAllPolicy( method.region, method.awsAccountId, method.restApiId, method.stage);
        return new AuthPolicy( principalId, policy, context, usageIdentifierKey );
    }

    public AuthPolicy denyPermission(String principalId) {
        return denyPermission(principalId, null, null);
    }

    public AuthPolicy denyPermission(String principalId, Map<String, String> context, String usageIdentifierKey ) {
        val method = getMethod();
        val policy = PolicyDocument.denyAllPolicy( method.region, method.awsAccountId, method.restApiId, method.stage);
        return new AuthPolicy( principalId, policy, context, usageIdentifierKey );
    }

    @Value public static class MethodArn {
        String region;
        String awsAccountId;
        String restApiId;
        String stage;
        String httpMethod;
        String resource;
    }
}