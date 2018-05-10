package lambdify.mojo;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import lombok.Getter;
import org.apache.maven.plugin.MojoExecutionException;

@Getter
public class ZipFileWriter implements AutoCloseable {

	static final String
		MESSAGE_CANT_CREATE_ZIP = "Can't create zip file",
		MESSAGE_CANT_ADD_TO_ZIP = "Can't add file to zip"
	;

	final List<String> prefixesToStripOutFromName = new ArrayList<>();
	final ZipOutputStream output;
	final String fileName;
	final String rootDirectory;

	public ZipFileWriter( final String fileName ) throws MojoExecutionException {
		this( fileName, "" );
	}

	public ZipFileWriter( final String fileName, final String rootDirectory ) throws MojoExecutionException {
		try {
			this.fileName = fileName;
			this.rootDirectory = rootDirectory;
			this.output = new ZipOutputStream( new FileOutputStream( fileName ) );
		} catch ( final FileNotFoundException e ) {
			throw new MojoExecutionException( MESSAGE_CANT_CREATE_ZIP, e );
		}
	}

	public void add( final String name ){
		add( name, null );
	}

	public void add( final String name, final InputStream content ) {
		try {
			final String fixedName = fixEntryName( name );
			if ( fixedName.isEmpty() ) return;

			output.putNextEntry( new ZipEntry( fixedName ) );

			if ( content != null ) {
				final byte[] bytes = new byte[1024];
				int length;
				while ((length = content.read(bytes)) >= 0)
					output.write(bytes, 0, length);
				output.closeEntry();
			}
		} catch ( final IOException e ) {
			throw new RuntimeException( MESSAGE_CANT_ADD_TO_ZIP, e );
		}
	}

	String fixEntryName( String entryName ) {
		for ( final String prefix : prefixesToStripOutFromName )
			entryName = entryName.replaceFirst( prefix, "" );
		final String finalEntryName = rootDirectory + "/" + entryName;
		return finalEntryName.replaceFirst( "^/", "" );
	}

	public void close() throws MojoExecutionException {
		try {
			output.close();
		} catch ( final IOException e ) {
			throw new MojoExecutionException( MESSAGE_CANT_CREATE_ZIP, e );
		}
	}

	public void stripPrefix( final String... prefixes ) {
		for ( final String prefix : prefixes )
			prefixesToStripOutFromName.add( prefix );
	}
}