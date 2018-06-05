package lambdify.core;

import java.io.*;
import java.lang.reflect.*;
import com.amazonaws.services.lambda.runtime.*;
import lombok.experimental.var;
import lombok.val;

/**
 *
 */
public abstract class LambdaStreamFunction<I, O> implements RequestStreamHandler {

	private Class<I> inputClass = findInputClass();

	@SuppressWarnings( "unchecked" )
	private Class<I> findInputClass()
	{
		var currentClass = (Class)getClass();
		var type = currentClass.getGenericSuperclass();
		while ( !Object.class.equals( currentClass )
			&& !isLambdaStreamFunctionType(type) ) {
			type = currentClass.getGenericSuperclass();
			currentClass = currentClass.getSuperclass();
		}

		if ( Object.class.equals( currentClass ) )
			throw new IllegalStateException( "Lambdify was unable to identify the Input type of this function" );
		return (Class<I>) ( (ParameterizedType) type ).getActualTypeArguments()[0];
	}

	private static boolean isLambdaStreamFunctionType( Type type ){
		return type instanceof ParameterizedType
			&& ( (ParameterizedType) type ).getRawType().equals( LambdaStreamFunction.class );
	}

	@SuppressWarnings( {"unchecked", "ConstantConditions"} )
	public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException
	{
		val serializer = LambdaConfig.INSTANCE.serializer();
		val inputObject = serializer.deserialize( input, inputClass );
		val outputObject = handleRequest( inputObject, context );
		serializer.serialize( outputObject, output );
	}

	protected abstract O handleRequest(I input, Context context);
}
