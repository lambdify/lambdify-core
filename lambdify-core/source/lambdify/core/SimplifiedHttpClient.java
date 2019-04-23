package lambdify.core;

import lombok.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Simplistic Http client designed as a wrapper over JDK's ${@link HttpURLConnection}.
 */
public class SimplifiedHttpClient implements Closeable {

    final static int BUFFER_SIZE = 1 << 16;
    final HttpURLConnection conn;

    public SimplifiedHttpClient( URL url, String method ) throws IOException {
        this( url, method, "application/json", "application/json" );
    }

    public SimplifiedHttpClient( URL url, String method, String requestContentType, String responseContentType ) throws IOException {
        conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod( method );
        requestHeader("Content-Type", requestContentType );
        requestHeader("Accept", responseContentType );
    }

    public Response sendAndReceive( @NonNull byte[] body ) throws IOException {
        write( body );
        return receive();
    }

    private void write( byte[] body ) throws IOException {
        try ( val requestChannel = new DataOutputStream(conn.getOutputStream()) ) {
            requestChannel.write( body );
        }
    }

    public Response receive() throws IOException {
        val status = conn.getResponseCode();

        val resp = status / 100 < 4
            ? readResponseFrom( conn.getInputStream() )
            : readResponseFrom( conn.getErrorStream() );

        return new Response( status, resp );
    }

    private byte[] readResponseFrom(InputStream inputStream) throws IOException {
        try {
            val contentLength = responseContentLength();
            if ( contentLength > -1 )
                return readBytesFromStream(inputStream, contentLength);
            return readBytesFromStream(inputStream);
        } finally {
            inputStream.close();
        }
    }

    static byte[] readBytesFromStream(InputStream inputStream, int length) throws IOException {
        val buffer = new byte[length];
        int read = 0, total = 0;

        while (read > -1 && total != length) {
            total+= read;
            read = inputStream.read(buffer, total, length - total);
        }

        if ( length > total )
            throw new IllegalStateException("Buffer poorly allocated. Read: " + total + "; Expected: " + length);

        return buffer;
    }

    static byte[] readBytesFromStream(InputStream inputStream) throws IOException {
        val buffer = new ByteArrayOutputStream(BUFFER_SIZE);
        int read;
        while (true) {
            read = inputStream.read();
            if ( read < 0 ) break;
            buffer.write( read );
        }
        return buffer.toByteArray();
    }

    private int responseContentLength() throws IOException {
        var header = responseHeader("Content-Length");
        if ( header == null )
            return -1;
        val pos = header.indexOf(';');
        if ( pos > -1 )
            header = header.substring(0, pos);
        return Integer.valueOf( header );
    }

    public void requestHeader( String name, String value ) {
        conn.setRequestProperty( name, value);
    }

    public String responseHeader( String name ) {
        var value = conn.getHeaderField( name );
        if ( value == null )
            value = conn.getHeaderField( name.toLowerCase() );
        return value;
    }

    @Override
    public void close() {
        conn.disconnect();
    }

    /**
     * Simplified representation of an HTTP response. Although it holds no header or other HTTP metadata, it
     * holds an eagerly read response body.
     */
    @Value public static class Response {
        final int status;
        final byte[] response;
    }
}