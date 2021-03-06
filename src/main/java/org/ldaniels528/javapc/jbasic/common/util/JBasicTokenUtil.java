package org.ldaniels528.javapc.jbasic.common.util;



import java.util.LinkedList;
import java.util.List;

import org.ldaniels528.javapc.jbasic.common.tokenizer.Token;
import org.ldaniels528.javapc.jbasic.common.tokenizer.TokenIterator;
import org.ldaniels528.javapc.jbasic.common.tokenizer.Tokenizer;
import org.ldaniels528.javapc.jbasic.common.tokenizer.TokenizerContext;
import org.ldaniels528.javapc.jbasic.common.tokenizer.parsers.DoubleQuotedTextTokenParser;
import org.ldaniels528.javapc.jbasic.common.tokenizer.parsers.EndOfLineTokenParser;
import org.ldaniels528.javapc.jbasic.common.tokenizer.parsers.NumericTokenParser;
import org.ldaniels528.javapc.jbasic.common.tokenizer.parsers.OperatorTokenParser;
import org.ldaniels528.javapc.jbasic.common.tokenizer.parsers.TextTokenParser;

import org.ldaniels528.javapc.jbasic.common.exceptions.JBasicException;
import org.ldaniels528.javapc.jbasic.common.exceptions.SyntaxErrorException;
import org.ldaniels528.javapc.jbasic.common.exceptions.TypeMismatchException;
import org.ldaniels528.javapc.jbasic.common.exceptions.UnexpectedTokenException;
import org.ldaniels528.javapc.jbasic.common.values.Value;
import org.ldaniels528.javapc.jbasic.gwbasic.values.GwBasicValues;

/**
 * GWBASIC Specific Tokenizer Utilities
 * @author lawrence.daniels@gmail.com
 */
public class JBasicTokenUtil {
	private static final Tokenizer tokenizer;
	
	static {
		tokenizer = new Tokenizer();
		tokenizer.add( new EndOfLineTokenParser() );	  
		tokenizer.add( new NumericTokenParser() );
		tokenizer.add( new OperatorTokenParser() );
		tokenizer.add( new DoubleQuotedTextTokenParser() );
		tokenizer.add( new TextTokenParser() );
	}
	
	  /**
	   * Mandates that the next token matches the supplied expected token
	   * @param it the given {@link TokenIterator token iterator}
	   * @param expected the expected token(s)
	   * @throws JBasicException
	   */
	  public static TokenIterator extractTokens( TokenIterator it, String ... expectedValues )
	  throws JBasicException {
		// create a container for return the extracted tokens
		final List<String> args = new LinkedList<String>();
		  
		// test/extract tokens 
	    for( final String expected : expectedValues ) {
	    		// retrieve the next token
	    		final String found = nextToken( it );
	    	
	    		// is it a flag?
	    		if( expected.equals( "@@" ) ) args.add( found );
	    		
	    		// if the expected and found values don't match, error ...
	    		else if( !found.equalsIgnoreCase( expected ) )
	    			throw new UnexpectedTokenException( expected, found );
	    }
	    
	    // return a new token iterator containing just our extracted arguments
	    return new TokenIterator( args );
	  }
	  
	  /**
	   * Mandates that the next token matches the supplied expected token
	   * @param it the given {@link TokenIterator token iterator}
	   * @param expected the expected token(s)
	   * @throws JBasicException
	   */
	  public static void mandateToken( TokenIterator it, String ... expectedValues )
	  throws JBasicException {    
		  // test each token
		  for( final String expected : expectedValues ) {
			  // retrieve the next token
			  final String found = nextToken( it );
	    	
			  // if the expected and found values don't match, error ...
			  if( !found.equalsIgnoreCase( expected ) )
	    			throw new UnexpectedTokenException( expected, found );
	    }
	  }

	  /**
	   * Returns the next string value from the iterator
	   * @param it the given {@link TokenIterator token iterator}
	   * @return the next string value in the iterator
	   * @throws JBasicException if there is not a next element
	   */
	  public static String nextToken( TokenIterator it ) throws JBasicException {
		  if( !it.hasNext() ) throw new SyntaxErrorException();
		  else return it.next();
	  }

	  /**
	   * Returns the next numeric value from the iterator
	   * @param it the given {@link TokenIterator token iterator}
	   * @return the next numeric value in the iterator
	   * @throws JBasicException if there is not a next element
	   */
	  public static double nextNumericToken( TokenIterator it )
	  throws JBasicException {
		  // get the next token
		  final String token = nextToken( it );
		  
		  // is it a numeric constant?
		  if( GwBasicValues.isNumericConstant( token ) ) {
			  return GwBasicValues.parseNumericString( token );  
		  }
		  
		  // is it a negative numeric constant?
		  else if( token.equals( "-" ) && 
				  it.hasNext() && GwBasicValues.isNumericConstant( it.peekAtNext() ) ) {
			  final String negNumber = token + it.next();		  
			  return GwBasicValues.parseNumericString( negNumber );
		  }
		  
		  // not numeric, error ...
		  throw new TypeMismatchException( token );
	  }

	  /**
	   * Insures that there are no more tokens left to read from the given iterator
	   * @param it the given {@link TokenIterator iterator}
	   * @throws JBasicException
	   */
	  public static void noMoreTokens( TokenIterator it ) throws JBasicException {
		  if( it.hasNext() ) 
			  throw new SyntaxErrorException();
	  }
	  
	  /** 
	   * Parses values from the given token iterator; insuring that the 
	   * minimum and maximum constraints are adhered to. 
	   * @param it the given {@link TokenIterator token iterator}
	   * @param minValues the given minimum number of values
	   * @param maxValues the given maximum number of values
	   * @return the extracted {@link Value values}
	   * @throws JBasicException
	   */
	  public static Value[] parseValues( TokenIterator it, int minValues, int maxValues ) 
	  throws JBasicException {
		  // create a container for the values
		  List<Value> values = new LinkedList<Value>();
		  
		  while( it.hasNext() ) {
			  // get the next value
			  Value value = GwBasicValues.getValueReference( it );			  
			  values.add( value );
			  
			  // if there's another argument ...
			  if( it.hasNext() ) {
				  // expect comma (,)
				  mandateToken( it, "," );
				  
				  // there must be another token
				  if( !it.hasNext() )
					  throw new SyntaxErrorException();
			  }
		  }
		  
		  // check the values
		  if( values.size() < minValues || values.size() > maxValues )
			  throw new SyntaxErrorException();
		  
		  // return the values
		  return values.toArray( new Value[ values.size() ] );
	  }
	  
	  /**
	   * Tokenizes the given text into sections
	   * @param text the given {@link String text}
	   * @return a {@link List list} of tokens
	   */
	  public static TokenIterator tokenize( String text ) {
		  // tokenize the statement
		  final TokenizerContext context = tokenizer.parse( text );
	    
		  // get the first token (keyword)
		  final List<String> list = new LinkedList<String>();
		  Token token = null;
		  while( ( token = tokenizer.nextToken( context ) ) != null ) {
			  list.add( token.getContent() );
		  }
		  return new TokenIterator( list );
	  }
	  
}
