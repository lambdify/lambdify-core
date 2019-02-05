package lambdify.core;

import java.io.*;
import com.fasterxml.jackson.jr.ob.JSON;
import lombok.*;

/**
 *
 */
public class SampleJsonSerializerAws implements AwsFunctionSerializer {

	@Override
	public byte[] serialize(Object deserializedBody) throws IOException {
		try {
			val bytes = new ByteArrayOutputStream();
			JSON.std.write( deserializedBody, bytes );
			return bytes.toByteArray();
		} catch ( IOException e ) {
			throw new IllegalStateException( e );
		}
	}

	@Override
	public <T> T deserialize(byte[] inputStream, Class<T> type) throws IOException {
		try {
			return JSON.std.beanFrom( type, inputStream );
		} catch ( IOException e ) {
			throw new IllegalStateException( e );
		}
	}
}
