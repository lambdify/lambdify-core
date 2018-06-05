package lambdify.core;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Map;
import com.amazonaws.services.lambda.runtime.Context;
import lambdify.core.SampleLambdaFunction.*;
import lombok.*;

/**
 *
 */
public class SampleLambdaFunction extends LambdaStreamFunction<Input, Output> {

	@Override
	public Output handleRequest(Input input, Context context) {
		assertNotNull( input );
		assertNotNull( input.map );
		assertEquals( "value", input.map.get( "key" ) );
		assertTrue( input.bool );
		assertEquals( 12.05, input.number, 0.000000001 );
		return new Output( "OK" );
	}

	@Data
	public static class Input {
		Map<String, String> map;
		boolean bool;
		double number;
	}

	@Value
	public static class Output {
		String status;
	}
}
