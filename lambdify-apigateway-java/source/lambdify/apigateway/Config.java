package lambdify.apigateway;

import static lombok.AccessLevel.PRIVATE;
import lombok.*;

/**
 *
 */
@NoArgsConstructor(access = PRIVATE)
public class Config {

	@NonNull String defaultContentType = "text/plain";
	@NonNull ParamReader defaultParamReader = new ParamReader();

	public static final Config INSTANCE = new Config();
}
