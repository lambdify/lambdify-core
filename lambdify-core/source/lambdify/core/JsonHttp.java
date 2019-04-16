package lambdify.core;

import lombok.NonNull;
import lombok.Value;
import lombok.val;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

class JsonHttp implements Closeable {

    final HttpURLConnection conn;

    JsonHttp( URL url, String method ) throws IOException {
        conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setRequestMethod( method );
    }

    JsonHttpResponse sendAndReceive( @NonNull byte[] body ) throws IOException {
        write( body );
        return receive();
    }

    private void write( byte[] body ) throws IOException {
        try ( val requestChannel = new DataOutputStream(conn.getOutputStream()) ) {
            requestChannel.write( body );
        }
    }

    JsonHttpResponse receive() throws IOException {
        val status = conn.getResponseCode();

        val resp = status / 100 < 4
            ? readResponseFrom( conn.getInputStream() )
            : readResponseFrom( conn.getErrorStream() );

        return new JsonHttpResponse( status, resp );
    }

    private byte[] readResponseFrom(InputStream inputStream) throws IOException {
        val length = conn.getHeaderField("Content-Length");
        val response = new byte[Integer.valueOf(length)];

        try (val responseChannel = new DataInputStream( inputStream ) ) {
            responseChannel.readFully(response);
            return response;
        }
    }

    String responseHeader( String name ) {
        return conn.getHeaderField( name );
    }

    @Override
    public void close() {
        conn.disconnect();
    }
}

@Value class JsonHttpResponse {
    final int status;
    final byte[] response;
}
