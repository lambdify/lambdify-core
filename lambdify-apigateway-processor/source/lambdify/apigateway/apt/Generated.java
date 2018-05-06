package lambdify.apigateway.apt;

import java.util.*;
import java.util.stream.Collectors;
import lambdify.apigateway.*;
import lambdify.apigateway.APIGateway.*;
import lambdify.apigateway.ann.*;
import lombok.*;

/**
 *
 */
public interface Generated {

	String DEFAULT_CONTENT_TYPE = Config.class.getCanonicalName() + ".INSTANCE.defaultContentType";

	@Data class Type
	{
		String canonicalName;
		List<Method> methods = new ArrayList<>();

		public String getPackageName(){
			return canonicalName.replaceAll( "(\\.[A-Z].*)", "" );
		}

		public String getSimpleName(){
			return canonicalName.replaceAll( ".*\\.([^.]+)$", "$1" );
		}

		public String getGeneratedSimpleName(){
			return getSimpleName() + "Router";
		}

		public MustacheIterable getMethodsIterable(){
			return new MustacheIterable( methods.subList( 1, methods.size() ) );
		}

		public String toString(){
			return "class " + canonicalName + "{\n\n"+ stringify( methods, "\n\n" ) +"\n\n}";
		}
	}

	@EqualsAndHashCode(callSuper = true)
	@Data class Method extends Element {

		boolean constructor;
		List<Element> parameters = new ArrayList<>();

		public DefinedRoute getRoute(){
			final Annotation annotation = getAnnotation( Route.class );
			if ( annotation == null )
				return null;
			final Map<String, Object> params = annotation.getParameters();
			return new DefinedRoute(
				params.get( "url" ), params.get( "method" )
			);
		}

		public boolean isVoidMethod(){
			return "void".equals( type );
		}

		public boolean isReturnsResponse(){
			return APIGateway.Response.class.getCanonicalName().equals( type );
		}

		public MustacheIterable getParameterIterable(){
			return new MustacheIterable( parameters );
		}

		public String getParameterList(){
			return stringify( parameters.stream().map( e -> e.type + " " + e.name ).collect( Collectors.toList() ) );
		}

		public String getArgumentList(){
			return stringify( parameters.stream().map( Element::getName ).collect( Collectors.toList() ) ) ;
		}

		public String toString(){
			return stringify( annotations, "\n" ) + "\n" + type + " " + name + "("+ stringify( parameters, "\n" ) +")";
		}
	}

	@Data class Element {
		String name;
		String type;
		List<Annotation> annotations = new ArrayList<>();

		public Annotation getPathParameter(){
			return getAnnotation( PathParam.class );
		}

		public Annotation getQueryParameter(){
			return getAnnotation( QueryParam.class );
		}

		public Annotation getHeaderParameter(){
			return getAnnotation( HeaderParam.class );
		}

		public boolean getBodyParameter(){
			return !isAPIGatewayRequest() && ( annotations.isEmpty() || getAnnotation( Body.class ) != null );
		}

		public boolean isAPIGatewayRequest(){
			return Request.class.getCanonicalName().equals( type );
		}

		protected Annotation getAnnotation( Class<?> clazz ) {
			for ( Annotation ann : annotations )
				if ( clazz.getCanonicalName().equals( ann.type ) )
					return ann;
			return null;
		}

		public String toString(){
			return stringify( annotations ) + " " + type + " " + name;
		}
	}

	@Data class Annotation {
		Element parent;
		String type;
		Map<String, Object> parameters = new HashMap<>();

		public Object getValue(){
			return parameters.get( "value" );
		}

		public String toString(){
			return "@" + type + "("+ stringify( parameters.entrySet() ) +")";
		}
	}

	@Data class MustacheIterable implements Iterable<MustacheWrapper>, Iterator<MustacheWrapper> {

		final List<?> data;
		int cursor = 0;

		@Override
		public Iterator<MustacheWrapper> iterator() {
			cursor = 0;
			return this;
		}

		@Override
		public boolean hasNext() {
			return cursor < data.size();
		}

		@Override
		public MustacheWrapper next() {
			return new MustacheWrapper( cursor == 0, data.get( cursor++ ) );
		}
	}

	@Value class MustacheWrapper {
		boolean first;
		Object data;
	}

	@Value class DefinedRoute {
		Object url, method;
	}

	static String stringify( Iterable<?> iterable ) {
		return stringify( iterable,", " );
	}

	static String stringify( Iterable<?> iterable, String delimiter ) {
		List<String> strings = new ArrayList<>(  );
		for ( Object param : iterable )
			strings.add( param.toString() );
		return String.join( delimiter, strings );
	}
}