package lambdify.apigateway;

import java.util.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PolicyDocument
{
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

	@NoArgsConstructor
	@AllArgsConstructor
	@Data
	public static class Statement {

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
}