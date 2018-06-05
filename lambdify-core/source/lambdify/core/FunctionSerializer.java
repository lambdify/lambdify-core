package lambdify.core;

import java.io.*;

/**
 * The Lambda function serializer. It is responsible for the whole serialization and
 * deserialization process that happens at request and response time of a Lambda function
 * execution.
 */
public interface FunctionSerializer {

	/**
	 * Serializes an object writing its output to the {@code outputStream}.
	 *
	 * @param unserializedBody
	 * @param outputStream
	 * @throws IOException
	 */
	void serialize(Object unserializedBody, OutputStream outputStream) throws IOException;

	/**
	 * Deserializes a stream by transforming it into a object which type corresponds
	 * to the {@code type} argument.
	 *
	 * @param inputStream
	 * @param type
	 * @param <T>
	 * @throws IOException
	 * @return
	 */
	<T> T deserialize(InputStream inputStream, Class<T> type) throws IOException;
}