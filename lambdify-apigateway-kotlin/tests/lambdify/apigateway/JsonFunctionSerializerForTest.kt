package lambdify.apigateway

import com.fasterxml.jackson.jr.ob.JSON
import lambdify.core.FunctionSerializer
import java.io.InputStream
import java.io.OutputStream

/**
 *
 */
class JsonFunctionSerializerForTest:FunctionSerializer {

    override fun <T : Any?> deserialize(inputStream: InputStream?, type: Class<T>?): T {
        return JSON.std.beanFrom(type, inputStream)
    }

    override fun serialize(unserializedBody: Any?, outputStream: OutputStream?) {
        JSON.std.write(unserializedBody, outputStream)
    }
}