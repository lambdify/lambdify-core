package lambdify.core;

import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Paths.get;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.*;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import lombok.*;
import org.junit.jupiter.api.Test;

/**
 *
 */
class LambdaStreamFunctionTest {

	final RequestStreamHandler handler = new SampleLambdaFunction();

	@SneakyThrows @Test
	void canHandleSampleRequest()
	{
		val expected = new String( readAllBytes( get("tests-resources/lambda-function-output.json") ) );
		try ( val input = new FileInputStream( "tests-resources/lambda-function-input.json" ) ) {
			val output = new ByteArrayOutputStream();
			handler.handleRequest( input, output, null );
			assertEquals( expected, output.toString() );
		}
	}
}