package lambdify.apigateway;

/**
 * Created by miere.teixeira on 18/04/2018.
 */
public enum Methods {
    GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS, ALL;

    public Router.Route and(String url ){
        return new Router.Route(url, this);
    }
}