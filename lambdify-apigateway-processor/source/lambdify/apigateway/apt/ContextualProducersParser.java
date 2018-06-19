package lambdify.apigateway.apt;

import static java.lang.String.format;
import java.util.*;
import java.util.function.Function;
import javax.lang.model.element.*;
import lambdify.aws.events.apigateway.ProxyRequestEvent;
import lombok.val;

/**
 *
 */
public class ContextualProducersParser {

	static final Function<String, String> FAIL = s -> { throw new IllegalArgumentException( "No producer defined for type " + s ); };

	static final String
		REQ_CANONICAL_NAME = ProxyRequestEvent.class.getCanonicalName(),
		MSG_INVALID_ARG = "Invalid %s method. Methods annotated with @Context should be static and should have a single parameter of type " + REQ_CANONICAL_NAME
	;

	final Map<String, String> producerCanonicalMethodName = new HashMap<>();

	void memorizeMethod( ExecutableElement method ) {
		val methodName = method.getSimpleName().toString();
		val isStatic = method.getModifiers().contains( Modifier.STATIC );
		val params = method.getParameters();
		val firstParameterCanonicalName = APT.getCanonicalName( params.get( 0 ) );

		if ( !isStatic || params.size() > 1 || !REQ_CANONICAL_NAME.equals( firstParameterCanonicalName ) )
			throw new IllegalArgumentException( format(MSG_INVALID_ARG, methodName ) );

		val classCanonicalName = APT.getCanonicalName( method.getEnclosingElement() );
		val methodCanonicalName = classCanonicalName +"."+ methodName;
		val returnTypeCanonicalName = APT.getCanonicalName( method.getReturnType().toString() );
		producerCanonicalMethodName.put( returnTypeCanonicalName, methodCanonicalName );
	}

	String getProducerFor( String type ) {
		return producerCanonicalMethodName.computeIfAbsent( type, FAIL );
	}
}
