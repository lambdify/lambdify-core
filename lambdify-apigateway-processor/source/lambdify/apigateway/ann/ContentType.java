package lambdify.apigateway.ann;

import java.lang.annotation.*;

/**
 * Customizes a route response. It should be used in case where you don't explicitly
 * defines an API Gateway Response as return of your functions.
 */
@Retention( RetentionPolicy.SOURCE )
@Target( ElementType.METHOD )
public @interface ContentType {

	String value();
}
