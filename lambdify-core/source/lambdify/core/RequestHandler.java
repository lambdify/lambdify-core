package lambdify.core;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import lombok.experimental.UtilityClass;
import lombok.val;
import lombok.var;

/**
 *
 */
public interface RequestHandler<I, O> extends RawRequestHandler {

	@Override
	default byte[] handle(byte[] input) throws IOException {
		val serializer = AwsLambdaConfig.INSTANCE.serializer();
		val inputObject = serializer.deserialize( input, getInputClass() );
		val outputObject = handleRequest( inputObject );
		return serializer.serialize( outputObject );
	}

	default Class<I> getInputClass() {
		return InputClass.get( getClass() );
	}

	O handleRequest(I inputObject);
}

@UtilityClass
@SuppressWarnings( "unchecked" )
class InputClass {

	final Map<Class, Class> cache = new HashMap<>();

	<I> Class<I> get( Class currentClass ) {
		return cache.computeIfAbsent( currentClass, InputClass::discovery );
	}

	private <I> Class<I> discovery( Class currentClass )
	{
		while ( !Object.class.equals( currentClass ) ) {
			var types = currentClass.getGenericInterfaces();
			for (val type : types)
				if (isLambdaStreamFunctionType(type))
					return (Class<I>) ((ParameterizedType) type).getActualTypeArguments()[0];
			currentClass = currentClass.getSuperclass();
		}

		throw new IllegalStateException( "Lambdify was unable to identify the Input type of this function" );
	}

	private boolean isLambdaStreamFunctionType( Type type ){
		return type instanceof ParameterizedType
			&& ( (ParameterizedType) type ).getRawType().equals( RequestHandler.class );
	}
}