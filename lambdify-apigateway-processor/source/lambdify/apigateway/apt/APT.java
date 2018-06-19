package lambdify.apigateway.apt;

import javax.lang.model.element.Element;

/**
 *
 */
interface APT {

	static String getCanonicalName( Element parameter ) {
		final String simpleName = parameter.asType().toString();
		return getCanonicalName( simpleName );
	}

	static String getCanonicalName( String simpleName ) {
		return simpleName.replaceAll( "<.*", "" );
	}
}
