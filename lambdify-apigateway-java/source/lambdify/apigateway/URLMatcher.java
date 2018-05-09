package lambdify.apigateway;

import java.util.*;
import java.util.function.BiFunction;
import lombok.*;

@Value
public class URLMatcher {

    @NonNull final List<CompiledEntry> compiled;

    public boolean matches(List<String> tokens, Map<String,String> ctx ) {
        if ( tokens.size() == compiled.size() ) {
            for (int i = 0; i < tokens.size(); i++) {
                val token = tokens.get(i);
                val entry = compiled.get(i);
                if (!entry.apply(token, ctx))
                    return false;
            }
            return true;
        }
        return false;
    }

    static URLMatcher compile(String url ) {
        val newTokens = new ArrayList<CompiledEntry>();
        for ( val it : tokenize(url) ) {
            newTokens.add(
                ( it.length() > 1 && it.charAt(1) == ':' )
                    ? new PlaceHolder(it.substring(2))
                    : new Equals(it) );
        }
        return new URLMatcher(newTokens);
    }

    static List<String> tokenize( @NonNull String path ) {
        if ( path.equals("/") ) return Collections.singletonList( "/" );
        else if ( path.isEmpty() ) return Collections.emptyList();
        else {
            val tokens = new ArrayList<String>();
            val url = (path.charAt(path.length()-1) != '/') ? path
                         : path.substring(0, path.length() - 1);
            for ( val it : url.split("/") ) {
                if (!it.isEmpty())
                    tokens.add("/"+it);
            }
            return tokens;
        }
    }

    @Value static class Equals implements CompiledEntry {

        @NonNull final String value;

        @Override
        public Boolean apply(String s, Map<String, String> stringStringMap) {
            return value.equals(s);
        }
    }

    @Value static class PlaceHolder implements CompiledEntry {

        @NonNull final String key;

        @Override
        public Boolean apply(String value, Map<String, String> ctx) {
            ctx.put(key, value.substring( 1 ));
            return true;
        }
    }

    interface CompiledEntry extends BiFunction<String, Map<String,String>, Boolean> {}
}
