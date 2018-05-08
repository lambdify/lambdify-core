package lambdify.apigateway;

import static lombok.AccessLevel.PRIVATE;
import lombok.*;
import lombok.experimental.Accessors;

/**
 *
 */
@Getter @Setter @Accessors(fluent = true)
@NoArgsConstructor(access = PRIVATE)
public class Config {

	public static final Config INSTANCE = new Config();

	@NonNull String defaultContentType = "text/plain";
	@NonNull ParamReader defaultParamReader = new ParamReader();
	@NonNull
	Router.LambdaFunction defaultNotFoundHandler = new DefaultNotFoundHandler();

	/**
	 * The default handler for cases where the request wasn't mapped and
	 * have no predefined response for it.
	 */
	class DefaultNotFoundHandler implements Router.LambdaFunction {

		@Override
		public Response invoke(Request input) {
			return Response.notFound();
		}
	}
}
