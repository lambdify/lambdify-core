package lambdify.apigateway.ann;

import java.lang.annotation.*;

/**
 *
 */
@Retention( RetentionPolicy.RUNTIME )
@Target({ElementType.PARAMETER, ElementType.METHOD})
public @interface Context {

}
