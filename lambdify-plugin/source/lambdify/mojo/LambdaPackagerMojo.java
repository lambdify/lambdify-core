package lambdify.mojo;

import java.io.*;
import java.net.*;

import lombok.*;
import org.apache.maven.plugin.*;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;

/**
 *
 */
@Mojo( name = "package", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME )
public class LambdaPackagerMojo extends AWSMojo {

	@Parameter( defaultValue = "${project}", required = true)
	@Getter MavenProject project;

	@Parameter( defaultValue = "true", required = true )
	@Getter Boolean enabled;

	@Parameter( defaultValue = "${project.build.directory}/zip-content", required = true )
	String outputDirectory;

	@Parameter( defaultValue = "${project.build.directory}/${project.build.finalName}.jar", required = true )
	String jarFileName;

	@Parameter( defaultValue = "${project.build.directory}/${project.build.finalName}.zip", required = true )
	String zipFileName;

	@Parameter( required = true )
	String handler;

	@Override
	protected void run() throws Exception {
		checkIfClassExists( handler );
		val packageFile = generatePackageFile();
		getLog().info( "Generated AWS Lambda package: " + packageFile );
	}

	private void checkIfClassExists(String handlerClass) throws MojoFailureException {
		try {
			getLog().info( "Checking handler class '" + handlerClass + "'..." );
			val cl = new URLClassLoader( getClassPathFor( jarFileName ), getClass().getClassLoader());
			cl.loadClass( handlerClass );
		} catch (MalformedURLException e) {
			throw new MojoFailureException(e.getMessage(),e);
		} catch (ClassNotFoundException e) {
			throw new MojoFailureException( "The specified class does not exists: " + handlerClass,e );
		}
	}

	private URL[] getClassPathFor( String jarFileName ) throws MalformedURLException {
		val classPathSize = project.getArtifacts().size();
		val classPath = new URL[ classPathSize + 1 ];
		var i = 1;
		for ( val artifact : project.getArtifacts() ) {
			classPath[i++] = artifact.getFile().toURI().toURL();
		}
		classPath[0] = new File( jarFileName ).toURI().toURL();
		return classPath;
	}

	private File generatePackageFile() throws IOException {
		try (
			val zipPackage = new ZipPackager( zipFileName, outputDirectory );
			val jarFile = new FileInputStream( jarFileName );
		) {
			zipPackage.copyDependenciesToZip( project );
			//zipPackage.copyFilesFromJarToZip( jarFileName );
			zipPackage.addExecutableFile( "lib/application.jar", jarFile );
			zipPackage.addExecutableFile("bootstrap", readEmbeddedFile( "/bootstrap" ) );
			zipPackage.addExecutableFile("META-INF/services/lambdify.core.RawRequestHandler", readEmbeddedFile( "/RawRequestHandler" ) );
		}
		return new File( zipFileName );
	}

	private InputStream readEmbeddedFile( String fileName ) throws IOException {
		val bootstrap = loadEmbeddedFile( fileName );
		val content = convertStreamToString(bootstrap)
				.replace( "[[main-class]]", handler );
		return new ByteArrayInputStream(content.getBytes());
	}

	private InputStream loadEmbeddedFile( String fileName ) throws IOException {
		val bootstrap = getClass().getResourceAsStream( fileName );
		if ( bootstrap == null ){
			val msg = "Failed to include default 'bootstrap' script.";
			throw new IOException( msg );
		}

		return bootstrap;
	}

	private static String convertStreamToString(java.io.InputStream is) {
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}
}
