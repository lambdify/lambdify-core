package lambdify.apigateway;

import lombok.*;
import lombok.experimental.var;

import javax.swing.plaf.nimbus.State;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by miere.teixeira on 18/04/2018.
 */
public interface APIGatewayAuthorizer {
    
    String
        VERSION = "Version",
        STATEMENT = "Statement",
        EFFECT = "Effect",
        ACTION = "Action",
        NOT_ACTION = "NotAction",
        RESOURCE = "Resource",
        NOT_RESOURCE = "NotResource",
        CONDITION = "Condition"
    ;
    
    @Value class AuthPolicy {

        String principalId;
        PolicyDocument policyDocument;
        Map<String, String> context;
        String usageIdentifierKey;

        public Map<String, Object> getPolicyDocument(){
            val serializablePolicy = new HashMap<String, Object>();
            serializablePolicy.put(VERSION, policyDocument.version);
            val statements = policyDocument.getStatements();
            serializablePolicy.put(STATEMENT, statements.stream()
                .map( this::statementToMap ).collect(Collectors.toList() ) );
            return serializablePolicy;
        }

        private Map<String, Object> statementToMap(Statement statement) {
            val serializableStatement = new HashMap<String, Object>();
            serializableStatement.put(EFFECT, statement.effect);
            serializableStatement.put(ACTION, statement.action);
            serializableStatement.put(RESOURCE, statement.getResources());
            serializableStatement.put(CONDITION, statement.condition);
            return serializableStatement;
        }
    }

    @NoArgsConstructor @AllArgsConstructor
    @Data class PolicyDocument {
        String version = "2012-10-17";
        String region;
        String awsAccountId;
        String restApiId;
        String stage;

        final Statement 
            allowStatement = Statement.emptyInvokeStatement("Allow"),
            denyStatement = Statement.emptyInvokeStatement("Deny");
        
        public List<Statement> getStatements(){
            val list = new ArrayList<Statement>();
            if ( !allowStatement.resources.isEmpty() )
                list.add( allowStatement );
            if ( !denyStatement.resources.isEmpty() )
                list.add( denyStatement );
            return list;
        }

        public void allowMethod( Methods method, String resource ) {
            addResourceToStatement(allowStatement, method, resource);
        }

        public void denyMethod( Methods method, String resource ) {
            addResourceToStatement(denyStatement, method, resource);
        }

        private void addResourceToStatement(Statement statement, Methods httpMethod, String resourcePath) {
            if (resourcePath.equals("/")) resourcePath = "";

            val resource = (resourcePath.startsWith("/")) ? resourcePath.substring(1) : resourcePath;
            val method = (httpMethod == Methods.ALL) ? "*" : httpMethod.toString();

            statement.addResource("arn:aws:execute-api:"+ region +":"+ awsAccountId +":"+ restApiId +"/"+ stage +"/"+ method +"/"+ resource +"");
        }

        public static PolicyDocument allowAllPolicy(String region, String awsAccountId, String restApiId, String stage) {
            return allowOnePolicy( region, awsAccountId, restApiId, stage, Methods.ALL, "*" );
        }

        public static PolicyDocument allowOnePolicy(String region, String awsAccountId, String restApiId, String stage, Methods method, String resourcePath){
            val policyDocument = new PolicyDocument("2012-10-17", region, awsAccountId, restApiId, stage);
            policyDocument.allowMethod(method, resourcePath);
            return policyDocument;
        }

        public static PolicyDocument denyAllPolicy(String region, String awsAccountId, String restApiId, String stage) {
            return denyOnePolicy( region, awsAccountId, restApiId, stage, Methods.ALL, "*" );
        }

        public static PolicyDocument denyOnePolicy(String region, String awsAccountId, String restApiId, String stage, Methods method, String resourcePath){
            val policyDocument = new PolicyDocument("2012-10-17", region, awsAccountId, restApiId, stage);
            policyDocument.denyMethod(method, resourcePath);
            return policyDocument;
        }
    }

    @NoArgsConstructor @AllArgsConstructor
    @Data class Statement {

        String effect, action;
        Map<String, Map<String, Object>> condition;
        List<String> resources;

        public void addResource( String resource ) { resources.add( resource ); }
        
        public void addCondition( String operator, String key, String value ) {
            condition.put( operator, Collections.singletonMap(key, value) );
        }

        public static Statement emptyInvokeStatement( String effect ) {
            return new Statement( effect, "execute-api:Invoke", new HashMap<>(), new ArrayList<>() );
        }
    }

    @Data class TokenAuthorizerContext {

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
    }

    @Value class MethodArn {
        String region;
        String awsAccountId;
        String restApiId;
        String stage;
        String httpMethod;
        String resource;
    }
}
