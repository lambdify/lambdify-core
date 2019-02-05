package lambdify.mojo;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import lombok.*;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.*;
import org.apache.maven.project.MavenProject;

/**
 *
 */
public class ZipPackager implements AutoCloseable {

	final ZipFileWriter zip;

	public ZipPackager( String fileName ) throws MojoExecutionException {
		zip = new ZipFileWriter( fileName );
	}

	void copyDependenciesToZip( MavenProject project ) throws MojoExecutionException {
		try {
			final Set<String> namesAlreadyIncludedToZip = new HashSet<>();
			for ( final Artifact artifact : (Set<Artifact>) project.getArtifacts() ) {
				final String artifactAbsolutePath = getArtifactAbsolutePath( artifact );
				if ( !namesAlreadyIncludedToZip.contains( artifactAbsolutePath ) ) {
					copyDependencyToZip( artifact, artifactAbsolutePath );
					namesAlreadyIncludedToZip.add( artifactAbsolutePath );
				}
			}
		} catch ( IOException cause ) {
			throw new MojoExecutionException( "Can't copy dependencies to zip", cause );
		}
	}

	String getArtifactAbsolutePath( final Artifact artifact ) {
		return artifact.getFile().getAbsolutePath();
	}

	void copyDependencyToZip(
			final Artifact artifact,
			final String artifactAbsolutePath ) throws IOException
	{
		if ( artifact.getScope().equals( "provided" ) )
			return;

		val jarName = "lib/" + artifact.getArtifactId() + "." + artifact.getType();
		try ( val inputStream = new FileInputStream( artifactAbsolutePath ) ) {
			addFile( jarName, inputStream );
		}
	}

	void copyFilesFromJarToZip( final String path ) throws MojoExecutionException {
		try ( ZipFileReader reader = new ZipFileReader( path.replace( "%20", " " ) ) ) {
			reader.read( zip::add );
		} catch ( IOException e ) {
			throw new MojoExecutionException( "Can't read " + path, e );
		}
	}

	void addFile( String fileName, InputStream content ) {
		zip.add( fileName, content );
	}

	@Override
	public void close() throws MojoExecutionException {
		zip.close();
	}
}
