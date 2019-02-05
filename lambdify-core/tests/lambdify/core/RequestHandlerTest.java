package lambdify.core;

import lombok.*;
import org.junit.jupiter.api.*;

import java.io.*;

import static java.nio.file.Files.*;
import static java.nio.file.Paths.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
class RequestHandlerTest {

	final RequestHandler<SampleLambdaFunction.Input, SampleLambdaFunction.Output>
			sampleLambdaFunction = new SampleLambdaFunction(),
			inheritedLambdaFunction = new InheritedLambdaFunction();

	@SneakyThrows @Test
	void sampleLambdaFunction_canHandleSampleRequest()
	{
		val expected = new String( readFile("tests-resources/lambda-function-output.json") );
		val input = readFile( "tests-resources/lambda-function-input.json" );
		val output = sampleLambdaFunction.handle( input );
		assertEquals( expected, new String( output ) );
	}

	@SneakyThrows @Test
	void inheritedLambdaFunction_canHandleSampleRequest()
	{
		val expected = new String( readFile("tests-resources/lambda-function-output.json") );
		val input = readFile( "tests-resources/lambda-function-input.json" );
		val output = inheritedLambdaFunction.handle( input );
		assertEquals( expected, new String( output ) );
	}

	byte[] readFile( String name ) throws IOException {
		return readAllBytes( get(name) );
	}
}