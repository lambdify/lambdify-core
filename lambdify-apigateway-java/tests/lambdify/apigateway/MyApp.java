package lambdify.apigateway;

import static java.util.Collections.*;
import static lambdify.apigateway.Methods.*;
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

    Response customNotFoundHandler( Request request ) {
        return Response.notFound()
            .setHeaders( singletonMap( "X-Custom", "Not Found" ) );
    }

    Response retrieveUsers( Request request ) {
        return Response.ok( singletonList(new User("User") ), "application/json" );
    }

    Response retrieveSingleUser( Request request ) {
        return Response.ok( "{'name':'Lambda User'}", "application/json" );
    }

    Response createReportOfUsers(){
        return Response.created();
    }

    void saveUser( Request request ) {}

    Response updateUser(Request request ){
        val user = request.getBodyAs( User.class );
        return Response.ok( user.getName(), "plain/text" );
    }

    @NoArgsConstructor
    @RequiredArgsConstructor
    @Getter @Setter public static class User {
        @NonNull String name;
    }
}