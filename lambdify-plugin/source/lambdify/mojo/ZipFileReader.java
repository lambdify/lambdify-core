package lambdify.mojo;

import java.io.*;
import java.util.Enumeration;
import java.util.function.BiConsumer;
import java.util.zip.*;
import org.apache.maven.plugin.MojoExecutionException;

public class ZipFileReader implements AutoCloseable {

	static final String MESSAGE_CANT_OPEN_ZIP = "Can't open zip file";
	final ZipFile zip;

	public ZipFileReader( final String zipFileName ) throws IOException {
		try {
			zip = new ZipFile( zipFileName );
		} catch ( final IOException e ) {
			throw new IOException( MESSAGE_CANT_OPEN_ZIP, e );
		}
	}

	public void read( final BiConsumer<String, InputStream> listener ) throws IOException {
		final Enumeration<? extends ZipEntry> entries = zip.entries();
		while ( entries.hasMoreElements() ) {
			final ZipEntry entry = entries.nextElement();
			final InputStream inputStream = zip.getInputStream( entry );
			listener.accept( entry.getName(), inputStream );
		}
	}

	public void close() throws IOException {
		try {
			zip.close();
		} catch ( final IOException e ) {
			throw new IOException( MESSAGE_CANT_OPEN_ZIP, e );
		}
	}
}