package lambdify.apigateway;

import lombok.Value;

/**
 * Content serializer for APIGateway.
 */
public interface Serializer {

	String contentType();

	Stringified toString(Object unserializedBody);

	<T> T toObject(String content, Class<T> type, boolean isBase64Encoded);

	/**
	 * A stringified representation of a serialized object.
	 */
	@Value class Stringified {

		final String content;
		final boolean isBase64Encoded;

		/**
		 * Creates a String representation for non base64-encoded content.
		 *
		 * @param content
		 * @return
		 */
		public static Stringified plainText(String content) {
			return new Stringified( content, false );
		}

		/**
		 * Creates a String representation for base64-encoded content.<br>
		 * <b>Note</b>: It won't serialize your content as base64. Actually, it expects
		 * that your content is already encoded as Base64.
		 *
		 * @param base64Content
		 * @return
		 */
		public static Stringified base64Content(String base64Content) {
			return new Stringified( base64Content, true );
		}
	}
}