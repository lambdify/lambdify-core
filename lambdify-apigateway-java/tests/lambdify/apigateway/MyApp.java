package lambdify.apigateway;

import static java.util.Collections.*;
import static lambdify.apigateway.Methods.*;
import lambdify.aws.events.apigateway.*;
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

    ProxyResponseEvent customNotFoundHandler(ProxyRequestEvent request ) {
        return Responses.notFound()
            .withHeaders( singletonMap( "X-Custom", "Not Found" ) );
    }

    ProxyResponseEvent retrieveUsers( ProxyRequestEvent request ) {
        return Responses.ok( singletonList(new User("User") ), "application/json" );
    }

    ProxyResponseEvent retrieveSingleUser( ProxyRequestEvent request ) {
        return Responses.ok( "{'name':'Lambda User'}", "application/json" );
    }

    ProxyResponseEvent createReportOfUsers(){
        return Responses.created();
    }

    void saveUser( ProxyRequestEvent request ) {}

    ProxyResponseEvent updateUser( ProxyRequestEvent request ){
        val user = RequestParameterReader.getBodyAs( request, User.class );
        return Responses.ok( user.getName(), "plain/text" );
    }

    @NoArgsConstructor
    @RequiredArgsConstructor
    @Getter @Setter public static class User {
        @NonNull String name;
    }
}