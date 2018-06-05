package lambdify.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import lombok.val;
import org.junit.jupiter.api.Test;

/**
 *
 */
class LambdaConfigTest {

	@Test void configCanFindTheSerializerOnTheClasspath(){
		val serializer = LambdaConfig.INSTANCE.serializer();
		assertNotNull( serializer );
		assertEquals( SampleJsonSerializer.class, serializer.getClass() );
	}
}