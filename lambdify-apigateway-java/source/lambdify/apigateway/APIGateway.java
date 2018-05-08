package lambdify.apigateway;

import java.util.*;
import lombok.*;

/**
 * A simplified APIGateway API. Contains all information required to
 * handle APIGateway requests received on your AWS Lambda functions.
 *
 * Created by miere.teixeira on 18/04/2018.
 */
public interface APIGateway {

    /**
     * Represents an API Gateway request.
     */
    @Data
    class Request {
        String resource;
        String path;
        String httpMethod;
        Map<String, String> headers;
        Map<String, String> queryStringParameters;
        Map<String, String> pathParameters;
        Map<String, String> stageVariables;
        RequestContext requestContext;
        String body;
        boolean isBase64Encoded;

        @Setter( AccessLevel.PACKAGE )
        transient Serializer serializer;

        String getContentType(){
            return getHeaders().get( "Content-Type" );
        }

        public Map<String, String> getHeaders(){
            if ( headers == null )
                headers = new HashMap<>();
            return headers;
        }

        public <T> T getHeader( String key, Class<T> clazz ) {
            val value = getHeaders().get( key );
            return Config.INSTANCE.defaultParamReader.convert( value, clazz );
        }

        public Map<String, String> getQueryStringsParameters(){
            if ( queryStringParameters == null )
                queryStringParameters = new HashMap<>();
            return queryStringParameters;
        }

        public <T> T getQueryParam( String key, Class<T> clazz ) {
            val value = getQueryStringParameters().get( key );
            return Config.INSTANCE.defaultParamReader.convert( value, clazz );
        }

        public Map<String, String> getPathParameters(){
            if ( pathParameters == null )
                pathParameters = new HashMap<>();
            return pathParameters;
        }

        public <T> T getPathParam( String key, Class<T> clazz ) {
            val value = getPathParameters().get( key );
            return Config.INSTANCE.defaultParamReader.convert( value, clazz );
        }

        public Map<String, String> getStageVariables(){
            if ( stageVariables == null )
                stageVariables = new HashMap<>();
            return stageVariables;
        }

        public <T> T getStageParam( String key, Class<T> clazz ) {
            val value = getStageVariables().get( key );
            return Config.INSTANCE.defaultParamReader.convert( value, clazz );
        }

        public <T> T getBodyAs( Class<T> type ) {
            if ( serializer == null )
                throw new RuntimeException( "Could not found a valid serializer for this request." );
            return serializer.toObject( getBody(), type, isBase64Encoded );
        }
    }

    @Data class RequestContext {
        String apiId;
        String accountId;
        Map<String, String> authorizer;
        Map<String, String> error;
        RequestContextIdentity identity;
        String path;
        String resourceId;
        String stage;
        String requestId;
        String resourcePath;
        String httpMethod;
    }

    @Data class RequestContextIdentity {
        String accountId;
        String apiKey;
        String apiKeyId;
        String caller;
        String cognitoIdentityPoolId;
        String cognitoIdentityId;
        String cognitoAuthenticationType;
        String cognitoAuthenticationProvider;
        String sourceIp;
        String userArn;
        String userAgent;
        String user;
    }

    /**
     * Represents an API Gateway response.
     */
    @Data class Response {

        int statusCode = 200;
        Map<String, String> headers;
        String body;
        boolean isBase64Encoded = false;

        @Getter( AccessLevel.NONE )
        @Setter( AccessLevel.NONE )
        transient Object unserializedBody;

        @Getter( AccessLevel.PACKAGE )
        @Setter( AccessLevel.NONE )
        transient String contentType;

        public Response setContentType( String contentType ){
            if ( headers == null )
                headers = new HashMap<>();
            headers.put( "Content-Type", contentType );
            this.contentType = contentType;
            return this;
        }

        boolean requiresSerialization(){
            return this.unserializedBody != null;
        }

        /**
         * Defines a serialized body.
         *
         * @param body
         * @return
         */
        public Response setBody(String body ){
            this.body = body;
            this.unserializedBody = null;
            return this;
        }

        /**
         * Defines an unserializer body. This object will be serialized right before the
         * a response is send back to the HTTP Client.
         *
         * @param body
         * @return
         */
        public Response setBody( Object body ){
            this.body = null;
            this.unserializedBody = body;
            return this;
        }

        public static Response notFound(){
            return new Response().setStatusCode(404).setContentType( "text/plain");
        }

        public static Response noContent(){
            return new Response().setStatusCode(204).setContentType( "text/plain");
        }

        public static Response created(){
            return new Response().setStatusCode(201).setContentType( "text/plain");
        }

        public static Response accepted(){
            return new Response().setStatusCode(202).setContentType( "text/plain");
        }

        public static Response ok(){
            return new Response().setStatusCode(200).setContentType( "text/plain");
        }

        public static Response ok(String body) {
            return ok(body, "text/plain");
        }

        public static Response ok(String body, String contentType ){
            return new Response().setStatusCode(200).setBody(body).setContentType( contentType );
        }

        public static Response ok(Object body) {
            return ok(body, "text/plain");
        }

        public static Response ok(Object body, String contentType ){
            return new Response().setStatusCode(200).setBody(body).setContentType( contentType );
        }

        public static Response with( int statusCode, Map<String, String> headers, String body ) {
            return new Response()
                .setStatusCode(statusCode)
                .setHeaders(headers)
                .setBody(body);
        }

        public static Response internalServerError(String errorMessage) {
            return new Response().setStatusCode(500).setBody(errorMessage).setContentType( "text/plain");
        }
    }

    /**
     * Content serializer for APIGateway.
     */
    interface Serializer {
        String contentType();

        Stringified toString(Object unserializedBody);

        <T> T toObject(String content, Class<T> type, boolean isBase64Encoded);

        /**
         * A stringified representation of a serialized object.
         */
        @Value class Stringified {
            final String content;
            final boolean isBase64Encoded;

            /**
             * Creates a String representation for non base64-encoded content.
             * @param content
             * @return
             */
            public static Stringified plainText( String content ) {
                return new Stringified( content, false );
            }

            /**
             * Creates a String representation for base64-encoded content.<br>
             * <b>Note</b>: It won't serialize your content as base64. Actually, it expects
             * that your content is already encoded as Base64.
             *
             * @param base64Content
             * @return
             */
            public static Stringified base64Content( String base64Content ) {
                return new Stringified( base64Content, true );
            }
        }
    }
}
