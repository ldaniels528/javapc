package org.ldaniels528.javapc.ibmpc.compiler.element.addressing;

import static org.ldaniels528.javapc.ibmpc.util.X86CompilerUtil.isNumber;

/**
 * 8086 Memory Address Parser
 * @author lawrence.daniels@gmail.com
 */
public class X86MemoryAddressParser {

	/**
	 * Indicates whether the given string represents a "referenced" address (e.g. '[BX+SI+45]').
	 * @return true, if the given string represents a "referenced" address.
	 */
	public static boolean isReferencedAddress( final String identifier ) {
		return identifier.startsWith( "[" ) && identifier.endsWith( "]" )
					&& !isNumber( identifier.substring( 1, identifier.length() - 1 ) );
	}

	/**
	 * Indicates whether the given string represents a direct memory address (e.g. '[2345]').
	 * @return true, if the given string represents a direct memory address.
	 */
	public static boolean isDirectAddress( final String identifier ) {
		return ( identifier.startsWith( "[" ) && identifier.endsWith( "]" )  
				&& isNumber( identifier.substring( 1, identifier.length() - 1 ) ) );
	}

}
