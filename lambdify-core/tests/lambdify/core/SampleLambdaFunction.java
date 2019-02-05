package lambdify.core;

import lambdify.core.SampleLambdaFunction.*;
import lombok.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class SampleLambdaFunction implements RequestHandler<Input, Output> {

	@Override
	public Output handleRequest(Input input) {
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

class InheritedLambdaFunction extends SampleLambdaFunction {
}