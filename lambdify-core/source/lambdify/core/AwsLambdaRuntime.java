package lambdify.core;

import lombok.Value;
import lombok.val;

import java.io.*;
import java.net.URL;
import java.util.*;

public class AwsLambdaRuntime {

    private final String lambdaServiceHost = System.getenv( "AWS_LAMBDA_RUNTIME_API" );
    private final RawRequestHandler requestHandler;

    public AwsLambdaRuntime( Iterable<RawRequestHandler> requestHandlers ) {
        val iterator = requestHandlers.iterator();

        if ( !iterator.hasNext() )
            throw new IllegalArgumentException( "No RawRequestHandler implementation defined" );

        this.requestHandler = iterator.next();
    }

    @SuppressWarnings("InfiniteLoopStatement")
    public void mainLoop() throws Exception {
        while ( true ) {
            val request = readRequest();
            try {
                val resp = requestHandler.handle(request.getBody());
                sendResponse(request.getRequestId(), resp, "response");
            } catch ( Throwable cause ) {
                val buffer = new StringWriter();
                cause.printStackTrace( new PrintWriter(buffer) );
                sendResponse(request.getRequestId(), buffer.toString().getBytes(), "error");
            }
        }
    }

    private LambdaRequest readRequest() throws Exception {
        val url = "http://" + lambdaServiceHost + "/2018-06-01/runtime/invocation/next";
        try ( val req = new SimplifiedHttpClient( new URL(url), "GET" ) ){
            val resp = req.receive();
            ensureResponseIsValid(resp);

            val body = resp.getResponse();
            val requestId = req.responseHeader("Lambda-Runtime-Aws-Request-Id");
            return new LambdaRequest( requestId, body );
        }
    }

    private void sendResponse(String requestId, byte[] respBody, String type) throws Exception {
        val url = "http://" + lambdaServiceHost + "/2018-06-01/runtime/invocation/"+ requestId +"/" + type;
        try ( val post = new SimplifiedHttpClient( new URL(url), "POST" ) ) {
            val resp = post.sendAndReceive( respBody );
            ensureResponseIsValid(resp);
        }
    }

    private void ensureResponseIsValid( SimplifiedHttpClient.Response resp ){
        if ( resp.getStatus() / 100 > 2 )
            throw new AwsLambdaCommunicationFailure(
                resp.getStatus(),
                resp.getResponse()
            );
    }

    public static void main( String[] args ) throws Exception {
        val handlers = ServiceLoader.load( RawRequestHandler.class );
        val runtime = new AwsLambdaRuntime( handlers );
        runtime.mainLoop();
    }
}

@Value class LambdaRequest {
    final String requestId;
    final byte[] body;
}

class AwsLambdaCommunicationFailure extends AwsLambdaFailure {

    AwsLambdaCommunicationFailure( int status, byte[] msg ) {
        super( status + " -> " + new String( msg ) );
    }
}
