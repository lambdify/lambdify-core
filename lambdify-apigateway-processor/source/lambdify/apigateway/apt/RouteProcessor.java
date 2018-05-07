package lambdify.apigateway.apt;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.tools.JavaFileObject;
import com.github.mustachejava.*;
import lambdify.apigateway.ann.Route;
import lambdify.apigateway.apt.Generated.Type;

/**
 *
 */
@SupportedAnnotationTypes("lambdify.apigateway.ann.*")
public class RouteProcessor extends AbstractProcessor {

	static final String TEMPLATE_FILE = "META-INF/generated-class.mustache";

	final ClassParser classParser = new ClassParser();
	final MustacheFactory mustacheFactory = new DefaultMustacheFactory();
	final String content;

	{
		try {
			content = readTemplate();
		} catch ( Throwable e ) {
			e.printStackTrace();
			throw new IllegalStateException( "Failed to read template: " + e.getMessage(), e );
		}
	}

	private String readTemplate() throws IOException, URISyntaxException {
		final ClassLoader classLoader = getClass().getClassLoader();
		URL url = classLoader.getResource( "/" + TEMPLATE_FILE );
		if ( url == null )
			url = classLoader.getResource( TEMPLATE_FILE );
		if ( url == null )
			throw new FileNotFoundException( "Could not find " + TEMPLATE_FILE );
		try ( InputStream is = url.openStream() ) {
			return convertStreamToString( is );
		}
	}

	private static String convertStreamToString(java.io.InputStream is) {
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	/**
	 * We just return the latest version of whatever JDK we run on. Stupid?
	 * Yeah, but it's either that or warnings on all versions but 1. Blame Joe.
	 *
	 * PS: this method was copied from Project Lombok. ;)
	 */
	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.values()[SourceVersion.values().length - 1];
	}

	@Override
	public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
		final Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith( Route.class );
		if ( !elements.isEmpty() ) {
			for ( Element element : elements )
				classParser.memorizeMethod( (ExecutableElement) element );
			generateClasses();
		}

		return true;
	}

	private void generateClasses() {
		final Filer filer = processingEnv.getFiler();

		final Collection<Type> types = classParser.getTypes();
		for ( Type type : types )
			try {
				final String name = type.getPackageName() + "." + type.getGeneratedSimpleName();
				final JavaFileObject sourceFile = filer.createSourceFile( name );
				/*Writer string = new StringWriter();
				generateClass( type, string );
				System.out.println( string.toString() );*/
				try (final Writer writer = sourceFile.openWriter()) {
					generateClass( type, writer );
				}
			} catch ( IOException e ) {
				throw new IllegalStateException( e );
			}
	}

	private void generateClass( Type type, Writer writer ) throws IOException {
		final Mustache mustache = mustacheFactory.compile( new StringReader( content ), "generated.class" );
		mustache.execute( writer, type );
		writer.flush();
	}
}
