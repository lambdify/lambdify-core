package lambdify.apigateway;

/**
 *
 */
public class UnhandledException extends RuntimeException {

	public UnhandledException( Throwable cause ) {
		super( cause.getMessage(), cause, false, false );
	}
}
