package lambdify.apigateway.apt;

import static lambdify.apigateway.apt.APT.getCanonicalName;
import java.util.*;
import java.util.Map.Entry;
import javax.lang.model.element.*;
import javax.lang.model.element.Element;
import lambdify.apigateway.apt.Generated.*;

/**
 *
 */
public class RouterClassParser {

	Map<String, Type> cachedTypes = new HashMap<>();
	final ContextualProducersParser producersParser;

	public RouterClassParser(ContextualProducersParser producersParser) {
		this.producersParser = producersParser;
	}

	boolean containsClasses(){
		return !cachedTypes.isEmpty();
	}

	Collection<Type> getTypes(){
		final ArrayList<Type> types = new ArrayList<>( cachedTypes.values() );
		cachedTypes = new HashMap<>();
		return types;
	}

	void memorizeMethod( ExecutableElement method ) {
		TypeElement typeElement = (TypeElement) method.getEnclosingElement();
		Generated.Type type = cachedTypes.computeIfAbsent( typeElement.asType().toString(), t -> createTypeFrom( t, typeElement ) );
		type.methods.add( createMethod( (ExecutableElement) method ) );
	}

	private Type createTypeFrom(String canonicalName, TypeElement type) {
		final List<? extends Element> elements = type.getEnclosedElements();
		final Type newType = new Generated.Type()
				.setCanonicalName( canonicalName ).setMethods( new ArrayList<>() );
		for ( Element element : elements ) {
			if ( ElementKind.CONSTRUCTOR.equals( element.getKind() ) ){
				newType.methods.add( createMethod( (ExecutableElement) element ) );
			}
		}
		return newType;
	}

	private Method createMethod(ExecutableElement method )
	{
		final List<? extends VariableElement> parameters = method.getParameters();
		final List<Generated.Element> newParameters = new ArrayList<>();
		for ( VariableElement parameter : parameters )
			newParameters.add( createParameter( parameter ) );

		String name = method.getSimpleName().toString();
		final Generated.Method generatedMethod = (Generated.Method)new Generated.Method()
				.setConstructor( "<init>".equals( name ) )
				.setParameters( newParameters )
				.setType( method.getReturnType().toString() )
				.setName( name );

		return (Generated.Method)generatedMethod
			.setAnnotations( loadAnnotations( method, generatedMethod ) );
	}

	private Generated.Element createParameter( VariableElement parameter )
	{
		final String typeCanonicalName = getCanonicalName( parameter );
		final Generated.Element param = new Generated.Element()
				.setName( parameter.getSimpleName().toString() )
				.setType( typeCanonicalName );

		param.setAnnotations( loadAnnotations( parameter, param ) );

		if ( param.getContext() != null )
			param.setContextualProducer( producersParser.getProducerFor( typeCanonicalName ) );

		return param;
	}

	private List<Generated.Annotation> loadAnnotations( Element element, Generated.Element parent )
	{
		final List<Generated.Annotation> newAnnotations = new ArrayList<>();
		final List<? extends AnnotationMirror> annotations = element.getAnnotationMirrors();
		for ( AnnotationMirror annotation : annotations )
			newAnnotations.add( createAnnotation( annotation ).setParent( parent ) );

		return newAnnotations;
	}

	private Generated.Annotation createAnnotation( AnnotationMirror annotation ) {
		final String type = annotation.getAnnotationType().asElement().asType().toString();
		final Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = annotation.getElementValues();
		final Map<String, Object> values = new HashMap<>();
		for ( Entry<? extends ExecutableElement, ? extends AnnotationValue> e : elementValues.entrySet() ) {
			values.put( e.getKey().getSimpleName().toString(), e.getValue().toString() );
		}
		return new Generated.Annotation()
				.setParameters( values ).setType( type );
	}

}
