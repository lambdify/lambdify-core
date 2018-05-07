package lambdify.apigateway.apt;

import static com.google.testing.compile.Compiler.javac;
import static org.junit.Assert.*;
import java.io.*;
import java.nio.file.*;
import java.util.Locale;
import javax.annotation.processing.Processor;
import javax.tools.*;
import com.google.common.collect.ImmutableList;
import com.google.testing.compile.*;
import lombok.*;
import lombok.launch.LombokProcessors;
import org.junit.jupiter.api.Test;

/**
 *
 */
public class RouteProcessorTest {

	final File
		classFile = new File( "tests/lambdify/apigateway/apt/MyAnnotatedResource.java" ),
		expectedGeneratedFile = new File("tests-resources/expected-generated.file" );

	@Test
	void canCompileAndGenerateTheExpectedRouter() throws IOException {
		val processor = new RouteProcessor();
		val compilation = compile( processor, classFile );
		assertNotNull( compilation );

		val generated = compilation.generatedSourceFiles().get( 0 );
		val generatedContent = generated.getCharContent( true ).toString();
		val expectedContent = new String( Files.readAllBytes( expectedGeneratedFile.toPath() ) );
		assertEquals( expectedContent, generatedContent );
	}

	@SneakyThrows
	private Compilation compile(Processor processor, File classFile) {
		val compiler = javac()
			.withProcessors( LombokProcessors.getProcessors() )
			.withProcessors( processor );
		val compilation = compiler.compile( JavaFileObjects.forResource( classFile.toURI().toURL() ) );
		print( compilation.errors() );
		print( compilation.diagnostics() );
		print( compilation.notes() );
		print( compilation.warnings() );
		return compilation;
	}

	private void print(ImmutableList<Diagnostic<? extends JavaFileObject>> logs ) {
		logs.forEach( d -> {
			System.out.println( String.format( "%s %s (%s:%s)\n\t>%s", d.getKind(), d.getMessage( Locale.ENGLISH ), d.getLineNumber(), d.getColumnNumber(), d.getCode() ) );
		});
	}
}
