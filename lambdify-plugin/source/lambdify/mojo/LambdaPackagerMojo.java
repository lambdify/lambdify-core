package lambdify.mojo;

import java.io.File;
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

	private File generatePackageFile() throws MojoExecutionException {
		try ( val zipPackage = new ZipPackager( zipFileName ) ) {
			zipPackage.copyDependenciesToZip( project );
			zipPackage.copyFilesFromJarToZip( jarFileName );
		}
		return new File( zipFileName );
	}
}
