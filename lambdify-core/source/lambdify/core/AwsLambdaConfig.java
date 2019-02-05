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
public class AwsLambdaConfig {

	public static final AwsLambdaConfig INSTANCE = new AwsLambdaConfig();

	@NonNull
	AwsFunctionSerializer serializer = loadDefaultSerializer();

	@SuppressWarnings( "LoopStatementThatDoesntLoop" )
	private AwsFunctionSerializer loadDefaultSerializer() {
		val serializers = ServiceLoader.load( AwsFunctionSerializer.class );
		for ( AwsFunctionSerializer serializer : serializers ) {
			return serializer;
		}
		return null;
	}

}
