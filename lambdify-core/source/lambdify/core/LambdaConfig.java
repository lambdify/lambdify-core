package lambdify.core;

import static lombok.AccessLevel.PRIVATE;
import java.util.ServiceLoader;
import lombok.*;
import lombok.experimental.Accessors;

/**
 *
 */
@ToString
@Getter
@Setter @Accessors(fluent = true)
@NoArgsConstructor(access = PRIVATE)
public class LambdaConfig {

	public static final LambdaConfig INSTANCE = new LambdaConfig();

	@NonNull FunctionSerializer serializer = loadDefaultSerializer();

	@SuppressWarnings( "LoopStatementThatDoesntLoop" )
	private FunctionSerializer loadDefaultSerializer() {
		val serializers = ServiceLoader.load( FunctionSerializer.class );
		for ( FunctionSerializer serializer : serializers ) {
			return serializer;
		}
		return null;
	}

}
