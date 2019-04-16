package lambdify.mojo;

import lombok.*;
import org.apache.maven.artifact.*;
import org.apache.maven.project.*;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;
import java.util.stream.*;

/**
 *
 */
public class ZipPackager implements AutoCloseable {

	final Path outputTmpFolder;
	final String fileName;

	public ZipPackager( String fileName, String outputTmpFolder ) {
		this.fileName = fileName;
		this.outputTmpFolder = Paths.get(outputTmpFolder);
	}

	void copyDependenciesToZip( MavenProject project ) throws IOException {
		try {
			val namesAlreadyIncludedToZip = new HashSet<>();
			for ( val artifact : project.getArtifacts()) {
				val artifactAbsolutePath = getArtifactAbsolutePath( artifact );
				if ( !namesAlreadyIncludedToZip.contains( artifactAbsolutePath ) ) {
					copyDependencyToZip( artifact, artifactAbsolutePath );
					namesAlreadyIncludedToZip.add( artifactAbsolutePath );
				}
			}
		} catch ( IOException cause ) {
			throw new IOException( "Can't copy dependencies to zip", cause );
		}
	}

	private String getArtifactAbsolutePath(final Artifact artifact) {
		return artifact.getFile().getAbsolutePath();
	}

	private void copyDependencyToZip(
			final Artifact artifact,
			final String artifactAbsolutePath ) throws IOException
	{
		if ( artifact.getScope().equals( "provided" ) )
			return;

		val jarName = "lib/" + artifact.getArtifactId() + "." + artifact.getType();
		try ( val inputStream = new FileInputStream( artifactAbsolutePath ) ) {
			memorizeFileToBeIncludedInTheZip( jarName, inputStream );
		}
	}

	private void memorizeFileToBeIncludedInTheZip( String name, InputStream content ){
		memorizeFileToBeIncludedInTheZip(name, content, false);
	}

	private void memorizeFileToBeIncludedInTheZip( String name, InputStream content, boolean executable ) {
		val file = outputTmpFolder.resolve(name);
		val folder = file.toFile().getParentFile();
		if ( !folder.exists() && !folder.mkdirs() )
			throw new RuntimeException( "Cannot create directory: " + folder.toString() );

		if ( !Files.isDirectory(file) )
			try {
				Files.copy( content, file, StandardCopyOption.REPLACE_EXISTING );
				setPermissions( file, executable );
			} catch (IOException e) {
				throw new RuntimeException( "Cannot create file: " + file.toString(), e );
			}
	}

	private void setPermissions( Path path, boolean executable ) throws IOException {
		val permission = executable ? "rwxr-xr-x" : "rw-r--r--";
		val permissions = PosixFilePermissions.fromString( permission );
		Files.setPosixFilePermissions( path, permissions );
	}

	void addExecutableFile(String fileName, InputStream content ) {
		memorizeFileToBeIncludedInTheZip(fileName, content, true);
	}

	@Override
	public void close() throws IOException {
		val rootDir = outputTmpFolder.toFile().getAbsolutePath();

		val filesToBeCompressed = Files.walk( outputTmpFolder )
			.filter( f -> !Files.isDirectory( f ) )
			.map( f ->
				f.toFile().getAbsolutePath()
				 .replace( rootDir, "" )
				 .replaceFirst("/", "")
			)
			.toArray(String[]::new);

		val args = new String[filesToBeCompressed.length+1];
		args[0] = fileName;
		System.arraycopy( filesToBeCompressed, 0, args, 1, filesToBeCompressed.length );

		CommandRunner
			.command("zip", args )
			.workingDirectory( outputTmpFolder.toString() )
			.run()
		;
	}
}
