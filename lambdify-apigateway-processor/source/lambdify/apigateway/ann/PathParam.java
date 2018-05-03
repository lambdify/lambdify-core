package lambdify.apigateway.ann;

import java.lang.annotation.*;

/**
 *
 */
@Retention( RetentionPolicy.SOURCE )
@Target( ElementType.PARAMETER )
public @interface PathParam {

	String value();
}
