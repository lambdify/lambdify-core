package lambdify.apigateway;

import static lombok.AccessLevel.PRIVATE;
import java.util.*;
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
	@NonNull Iterable<Serializer> serializers;

	/**
	 * Lazy loader of {@code serializers}.
	 * @return
	 */
	public Iterable<Serializer> getSerializers() {
		if ( serializers == null ) {
			val loaded = new ArrayList<Serializer>();
			val found = ServiceLoader.load( Serializer.class );
			for ( val serialize : found )
				loaded.add( serialize );
			serializers = loaded;
		}
		return serializers;
	}

	/**
	 * The default handler for cases where the request wasn't mapped and
	 * have no predefined response for it.
	 */
	class DefaultNotFoundHandler implements LambdaFunction {

		@Override
		public Response invoke(Request input) {
			return Response.notFound();
		}
	}
}
