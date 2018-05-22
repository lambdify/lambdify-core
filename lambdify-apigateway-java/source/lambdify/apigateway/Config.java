package lambdify.apigateway;

import static lombok.AccessLevel.PRIVATE;
import java.util.*;
import com.amazonaws.services.lambda.runtime.events.*;
import lambdify.apigateway.Router.LambdaFunction;
import lombok.*;
import lombok.experimental.Accessors;

/**
 *
 */
@ToString
@Getter @Setter @Accessors(fluent = true)
@NoArgsConstructor(access = PRIVATE)
public class Config {

	public static final Config INSTANCE = new Config();

	/**
	 * The default value that would be used when a response is produced
	 * and no "Content-Type" was defined.
	 */
	@NonNull String defaultContentType = "text/plain";

	/**
	 * The default {@link LambdaFunction} to be invoked when the router was not
	 * able to find a route that matches the current request.
	 */
	@NonNull LambdaFunction defaultNotFoundHandler = new DefaultNotFoundHandler();

	/**
	 * The parameter reader responsible to convert String into specific objects.
	 */
	@NonNull ParamReader paramReader = new ParamReader();

	/**
	 * The request serializers. By default it loads all {@code Serializer}s found at the class path.
	 */
	@NonNull Map<String, Serializer> serializers;

	/**
	 * Lazy loader of {@code serializers}.
	 * @return
	 */
	public Map<String, Serializer> serializers() {
		if ( serializers == null ) {
			serializers = new HashMap<>();
			loadDefaultSerializers();
		}
		return serializers;
	}

	private void loadDefaultSerializers(){
		val found = ServiceLoader.load( Serializer.class );
		for ( val serializer : found ) {
			registerSerializer( serializer );
		}
	}

	public Config registerSerializer( Serializer serializer ) {
		val previous = serializers().put( serializer.contentType(), serializer );
		if ( previous != null )
			System.err.println( "Overriding previously registered serializer for " + serializer.contentType() );
		return this;
	}

	/**
	 * The default handler for cases where the request wasn't mapped and
	 * have no predefined response for it.
	 */
	class DefaultNotFoundHandler implements LambdaFunction {

		@Override
		public APIGatewayProxyResponseEvent invoke(APIGatewayProxyRequestEvent input) {
			return Responses.notFound();
		}
	}
}
