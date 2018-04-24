package samples.aws.lambda;

import com.amazonaws.services.lambda.runtime.*;
import lombok.extern.log4j.Log4j2;

/**
 * An endpoint that simply replies 'Hello World' for any request it receives.
 */
@Log4j2
public class HelloWorld implements RequestHandler<AmazonLambdaRequest, AmazonLambdaResponse> {

	@Override
	public AmazonLambdaResponse handleRequest(AmazonLambdaRequest amazonLambdaRequest, Context context) {
		log.info( "Request received: " + amazonLambdaRequest );
		return AmazonLambdaResponse.with( "Hello World!" );
	}
}
