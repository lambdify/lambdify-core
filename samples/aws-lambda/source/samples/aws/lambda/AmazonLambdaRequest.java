package samples.aws.lambda;

import java.util.Map;
import lombok.*;

/**
 * With the Lambda proxy integration, API Gateway maps the entire client request to the
 * input event parameter of the back-end Lambda function as defined on this class.
 */
@ToString
@SuppressWarnings("unchecked")
public class AmazonLambdaRequest {

    @Setter @Getter String resource;
    @Setter @Getter String path;
    @Setter @Getter String httpMethod;
    @Setter @Getter Map<String, String> headers;
    @Setter @Getter Map<String, String> queryStringParameters;
    @Setter @Getter Map<String, String> pathParameters;
    @Setter @Getter Map<String, String> stageVariables;
    @Setter @Getter RequestContext requestContext;
    @Setter @Getter String body;
    @Setter @Getter boolean isBase64Encoded;

	@Getter
	@Setter
	@ToString
	public static class RequestContext {
		String accountId;
		String resourceId;
		String stage;
		String requestId;
		RequestContextIdentity identity;
		String resourcePath;
		String httpMethod;
		String apiId;
	}

	@Getter
	@Setter
	@ToString
	public static class RequestContextIdentity {
		String cognitoIdentityPoolId;
		String accountId;
		String cognitoIdentityId;
		String caller;
		String apiKey;
		String sourceIp;
		String cognitoAuthenticationType;
		String cognitoAuthenticationProvider;
		String userArn;
		String userAgent;
		String user;
	}
}
