package lambdify.apigateway;

import java.time.*;
import java.util.*;
import java.util.function.Function;
import lombok.val;

/**
 *
 */
public class ParamReader {

	private Map<Class, Function<String, ?>> converters = new HashMap<>();

	{
		registerConverter( ZonedDateTime.class, s -> ZonedDateTime.parse( s ).withZoneSameInstant( ZoneOffset.UTC ) );
		registerConverter( LocalDate.class, LocalDate::parse );
		registerConverter( Byte.class, Byte::parseByte );
		registerConverter( byte.class, Byte::parseByte, Byte.valueOf( "0" ) );
		registerConverter( Short.class, Short::parseShort );
		registerConverter( short.class, Short::parseShort, Short.valueOf( "0" ));
		registerConverter( Long.class, Long::parseLong );
		registerConverter( long.class, Long::parseLong, Long.valueOf( "0" ) );
		registerConverter( Integer.class, Integer::parseInt );
		registerConverter( int.class, Integer::parseInt, Integer.valueOf( "0" ) );
		registerConverter( Double.class, Double::parseDouble );
		registerConverter( double.class, Double::parseDouble, Double.valueOf( "0" ) );
		registerConverter( Float.class, Float::parseFloat );
		registerConverter( float.class, Float::parseFloat, Float.valueOf( "0" ) );
		registerConverter( String.class, s -> s );
	}

	public <T> void registerConverter( Class<T> clazz, Function<String, T> converter, T defaultValue ) {
		registerConverter( clazz, s->{
			if ( s == null )
				return defaultValue;
			return converter.apply( s );
		});
	}

	public <T> void registerConverter( Class<T> clazz, Function<String, T> converter ) {
		converters.put( clazz, converter );
	}

	@SuppressWarnings( "unchecked" )
	public <T> T convert( String value, Class<T> clazz ) {
		val converter = converters.get( clazz );
		if ( converter == null )
			throw new RuntimeException( "No converter found for " + clazz );
		return (T)converter.apply( value );
	}
}
