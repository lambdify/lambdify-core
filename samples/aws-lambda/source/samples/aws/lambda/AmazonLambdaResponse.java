package samples.aws.lambda;

import java.util.*;
import lombok.*;
import lombok.experimental.Accessors;

/**
 *
 */
@Getter @Setter @Accessors(chain = true)
@RequiredArgsConstructor
public class AmazonLambdaResponse {

	private static final Map<String, String> DEFAULT_CONTENT_TYPE = Collections.singletonMap( "Content-Type", "text/plain" );

	int statusCode;
	Map<String, String> headers;
	String body;

	final boolean isBase64Encoded = false;

	public static AmazonLambdaResponse with( String body ) {
		return new AmazonLambdaResponse()
            .setStatusCode(200)
            .setHeaders(DEFAULT_CONTENT_TYPE)
            .setBody( body );
	}

	public static AmazonLambdaResponse noContent() {
		return AmazonLambdaResponse.create( 204 );
	}

	public static AmazonLambdaResponse notFound() {
		return AmazonLambdaResponse.create( 404 );
	}

    public static AmazonLambdaResponse notAuthenticated() {
		return AmazonLambdaResponse.create( 401 );
    }

    public static AmazonLambdaResponse create( int statusCode ) {
        return create( statusCode, Collections.emptyMap() );
    }

    public static AmazonLambdaResponse create( int statusCode, Map<String, String> headers ) {
        return new AmazonLambdaResponse()
            .setStatusCode( statusCode )
            .setHeaders( headers );
    }
}
