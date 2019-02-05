package lambdify.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import lombok.val;
import org.junit.jupiter.api.Test;

/**
 *
 */
class AwsLambdaConfigTest {

	@Test void configCanFindTheSerializerOnTheClasspath(){
		val serializer = AwsLambdaConfig.INSTANCE.serializer();
		assertNotNull( serializer );
		assertEquals( SampleJsonSerializerAws.class, serializer.getClass() );
	}
}