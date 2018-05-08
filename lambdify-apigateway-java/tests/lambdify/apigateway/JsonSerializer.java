package lambdify.apigateway;

import java.io.IOException;
import com.fasterxml.jackson.jr.ob.JSON;
import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public class JsonSerializer implements Serializer {

    @Getter String contentType = "application/json";

    @Override
    public Stringified toString(Object unserializedBody) {
        try {
            return Stringified.plainText( JSON.std.asString(unserializedBody) );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T toObject(String content, Class<T> type, boolean isBase64Encoded) {
        try {
            return JSON.std.beanFrom(type, content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
