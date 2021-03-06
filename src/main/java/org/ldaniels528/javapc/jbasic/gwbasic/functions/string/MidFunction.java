package org.ldaniels528.javapc.jbasic.gwbasic.functions.string;

import org.ldaniels528.javapc.ibmpc.devices.memory.MemoryObject;
import org.ldaniels528.javapc.ibmpc.devices.memory.StringMemoryObject;
import org.ldaniels528.javapc.jbasic.common.JBasicCompiledCodeReference;
import org.ldaniels528.javapc.jbasic.common.exceptions.JBasicException;
import org.ldaniels528.javapc.jbasic.common.functions.JBasicFunction;
import org.ldaniels528.javapc.jbasic.common.values.types.impl.JBasicTempString;

import org.ldaniels528.javapc.jbasic.common.tokenizer.TokenIterator;

/**
 * MID$ Function
 * <br>Syntax: MID$(<i>var$</i>,<i>start</i>[,<i>length</i>])
 */
public class MidFunction extends JBasicFunction {
	private static final int[] PARAM_TYPES = new int[] { TYPE_STRING, TYPE_NUMERIC, TYPE_NUMERIC };
	private static final int MIN_PARAMS = 2;

    /**
     * Creates an instance of this opCode
     * @param it the given {@link TokenIterator iterator}
     * @throws JBasicException
     */
    public MidFunction( String name, TokenIterator it ) throws JBasicException {
    		super( name, it, PARAM_TYPES, MIN_PARAMS );
    }

    /* 
     * (non-Javadoc)
     * @see org.ldaniels528.javapc.jbasic.values.Value#getValue(org.ldaniels528.javapc.jbasic.environment.JBasicEnvironment)
     */
    public MemoryObject getValue( JBasicCompiledCodeReference program ) {    		
    		// get the parameter values
		final MemoryObject[] objects = getParameterValues( program );
		
    	  	// get the source string, start, and length
		final String str = objects[0].toString();
		final int start = objects[1].toInteger();		
		final int length = ( objects.length > 2 ) ? objects[2].toInteger() : str.length();
		
		// calculate begin and end of substring
		final int begin = start - 1;
		final int end   = ( start + length <= str.length() ) ? start + length : str.length();
		
		// return the substring
		final StringMemoryObject string = new JBasicTempString();
		string.setString( substring( str, begin, end ) );    		
		return string;
    }
    
    /**
     * Extracts the substring from the given source string starting 
     * at the <i>start</i> position, and ending at the <i>end</i> position.
     * @param str the given source string
     * @param start the starting position
     * @param end the ending position
     * @return the extracted substring
     */
    private String substring( String str, int start, int end ) {
    		// if the start index is past the end, nothing ...
    		if( start > end ) return "";
    		// extract the substring
    		else return str.substring( start, end );
    }
    
}
