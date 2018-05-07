package lambdify.apigateway.ann;

import java.lang.annotation.*;
import lambdify.apigateway.Methods;

/**
 * Defines a route.
 */
@Retention( RetentionPolicy.SOURCE )
@Target( ElementType.METHOD )
public @interface Route {

	String url();

	Methods method();
}
