package lambdify.apigateway;

import com.fasterxml.jackson.jr.ob.JSON;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.IOException;

@Accessors(fluent = true)
public class JsonSerializer implements APIGateway.Serializer {

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
