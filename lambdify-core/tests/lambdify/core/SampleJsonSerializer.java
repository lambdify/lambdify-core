package lambdify.core;

import java.io.*;
import com.fasterxml.jackson.jr.ob.JSON;

/**
 *
 */
public class SampleJsonSerializer implements FunctionSerializer {

	@Override
	public void serialize(Object unserializedBody, OutputStream outputStream) {
		try {
			JSON.std.write( unserializedBody, outputStream );
		} catch ( IOException e ) {
			throw new IllegalStateException( e );
		}
	}

	@Override
	public <T> T deserialize(InputStream inputStream, Class<T> type) {
		try {
			return JSON.std.beanFrom( type, inputStream );
		} catch ( IOException e ) {
			throw new IllegalStateException( e );
		}
	}
}
