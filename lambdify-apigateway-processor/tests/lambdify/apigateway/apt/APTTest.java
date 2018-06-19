package lambdify.apigateway.apt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import lombok.val;
import org.junit.jupiter.api.Test;

/**
 *
 */
class APTTest {

	@Test void canConvertElementOfGenericTypeToCanonicalName(){
		val canonicalName = APT.getCanonicalName( "java.lang.Map<String, Object>" );
		assertEquals( "java.lang.Map", canonicalName );
	}

	@Test void canConvertElementOfRegularTypeToCanonicalName(){
		val canonicalName = APT.getCanonicalName( "java.lang.String" );
		assertEquals( "java.lang.String", canonicalName );
	}
}