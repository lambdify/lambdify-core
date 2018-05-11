package lambdify.apigateway;

import static java.util.Collections.*;
import static lambdify.apigateway.Methods.*;
import com.amazonaws.services.lambda.runtime.events.*;
import lombok.*;

public class MyApp extends App {{

    val users = new UserRepository();

    routes(
        GET.and( "/users" ).with( users::retrieveUsers ),
        GET.and("/users/:id" ).with( users::retrieveSingleUser ),
        PATCH.and("/users" ).with( users::createReportOfUsers ),
        POST.and("/users" ).withNoContent( users::saveUser )
    );
}}

class UserRepository {

    APIGatewayProxyResponseEvent customNotFoundHandler( APIGatewayProxyRequestEvent request ) {
        return Responses.notFound()
            .withHeaders( singletonMap( "X-Custom", "Not Found" ) );
    }

    APIGatewayProxyResponseEvent retrieveUsers( APIGatewayProxyRequestEvent request ) {
        return Responses.ok( singletonList(new User("User") ), "application/json" );
    }

    APIGatewayProxyResponseEvent retrieveSingleUser( APIGatewayProxyRequestEvent request ) {
        return Responses.ok( "{'name':'Lambda User'}", "application/json" );
    }

    APIGatewayProxyResponseEvent createReportOfUsers(){
        return Responses.created();
    }

    void saveUser( APIGatewayProxyRequestEvent request ) {}

    APIGatewayProxyResponseEvent updateUser( APIGatewayProxyRequestEvent request ){
        val user = RequestParameterReader.getBodyAs( request, User.class );
        return Responses.ok( user.getName(), "plain/text" );
    }

    @NoArgsConstructor
    @RequiredArgsConstructor
    @Getter @Setter public static class User {
        @NonNull String name;
    }
}