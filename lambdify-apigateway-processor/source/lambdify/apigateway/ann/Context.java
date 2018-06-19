package lambdify.apigateway.ann;

import java.lang.annotation.*;

/**
 *
 */
@Retention( RetentionPolicy.SOURCE )
@Target({ElementType.PARAMETER, ElementType.METHOD})
public @interface Context {

}
