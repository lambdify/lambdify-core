package lambdify.apigateway.apt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import lombok.val;
import org.junit.jupiter.api.Test;

/**
 *
 */
class ClassParserTest {

	@Test void canConvertElementOfGenericTypeToCanonicalName(){
		val canonicalName = ClassParser.getCanonicalName( "java.lang.Map<String, Object>" );
		assertEquals( "java.lang.Map", canonicalName );
	}

	@Test void canConvertElementOfRegularTypeToCanonicalName(){
		val canonicalName = ClassParser.getCanonicalName( "java.lang.String" );
		assertEquals( "java.lang.String", canonicalName );
	}
}