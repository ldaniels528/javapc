package org.ldaniels528.javapc.jbasic.gwbasic.program.commands.graphics;

import org.ldaniels528.javapc.ibmpc.devices.display.IbmPcDisplay;
import org.ldaniels528.javapc.ibmpc.devices.display.modes.IbmPcDisplayMode;
import org.ldaniels528.javapc.ibmpc.devices.memory.MemoryObject;
import org.ldaniels528.javapc.jbasic.common.exceptions.IllegalFunctionCallException;
import org.ldaniels528.javapc.jbasic.common.exceptions.JBasicException;
import org.ldaniels528.javapc.jbasic.common.exceptions.NotYetImplementedException;
import org.ldaniels528.javapc.jbasic.common.program.JBasicCompiledCode;
import org.ldaniels528.javapc.jbasic.common.util.JBasicTokenUtil;
import org.ldaniels528.javapc.jbasic.common.values.Value;
import org.ldaniels528.javapc.jbasic.gwbasic.program.commands.GwBasicCommand;
import org.ldaniels528.javapc.jbasic.gwbasic.values.GwBasicValues;

import org.ldaniels528.javapc.jbasic.common.tokenizer.TokenIterator;

/**
 * GET Command
 * <br>Syntax: GET (<i>x1</i>,<i>y1</i>)-(<i>x2</i>,<i>y2</i>),<i>imageName</i>
 * @author lawrence.daniels@gmail.com
 */
public class GetImageOp extends GwBasicCommand {
	private final Value xpos1;
	private final Value ypos1;
	private final Value xpos2;
	private final Value ypos2;
	private final Value image;

	  /**
	   * Creates an instance of this operation
	   * @param it the given {@link TokenIterator token iterator}
	   * @throws JBasicException
	   */
	  public GetImageOp( TokenIterator it ) throws JBasicException {    
		  // mandate the '(' symbol
		  JBasicTokenUtil.mandateToken( it, "(" );
			
		  // get the x-coordinate
		  xpos1 = GwBasicValues.getValueReference( it );
			
		  // mandate the ',' symbol
		  JBasicTokenUtil.mandateToken( it, "," );
			
		  // get the x-coordinate
		  ypos1 = GwBasicValues.getValueReference( it );
			
		  // mandate the ')' symbol
		  JBasicTokenUtil.mandateToken( it, ")" );			
			
		  // mandate the '-' symbol
		  JBasicTokenUtil.mandateToken( it, "-" );
			
		  // mandate the '(' symbol
		  JBasicTokenUtil.mandateToken( it, "(" );
			
		  // get the radius
		  xpos2 = GwBasicValues.getValueReference( it );

		  // mandate the "," symbol
		  JBasicTokenUtil.mandateToken( it, "," );						
			
		  // get xpos2
		  ypos2 = GwBasicValues.getValueReference( it );
			
		  // mandate the ')' symbol
		  JBasicTokenUtil.mandateToken( it, ")" );
			
		  // mandate the "," symbol
		  JBasicTokenUtil.mandateToken( it, "," );
			
		  // get the image reference
		  image = GwBasicValues.getValue( it );
				
		  // no more tokens
		  JBasicTokenUtil.noMoreTokens( it );
	  }

	  /* 
	   * (non-Javadoc)
	   * @see org.ldaniels528.javapc.jbasic.base.program.OpCode#execute(org.ldaniels528.javapc.jbasic.base.program.JBasicProgram, org.ldaniels528.javapc.jbasic.base.environment.JBasicEnvironment)
	   */
	  public void execute( JBasicCompiledCode program ) 
	  throws JBasicException {
		  // get the screen instance
		  final IbmPcDisplay screen = program.getSystem().getDisplay();
		  
		  // if current mode is not graphical, error
		  final IbmPcDisplayMode mode = screen.getDisplayMode();
		  if( !mode.isGraphical() )
			  throw new IllegalFunctionCallException( this );
		  
		  // get the parameters
		  final MemoryObject object = image.getValue( program );
		  final int x1 = xpos1.getValue( program ).toInteger();
		  final int y1 = ypos1.getValue( program ).toInteger();
		  final int x2 = xpos2.getValue( program ).toInteger();
		  final int y2 = ypos2.getValue( program ).toInteger();
		  
		  // get the image
		  captureImage( object, x1, y1, x2, y2 );		  
	  }
	  
	  /**
	   * Gets the contents of the given area (x1,y1)-(x2,y2) and
	   * places it into the given bitmap.
	   */
	  private void captureImage( final MemoryObject object,
			  				 final int x1,
			  				 final int y1,
			  				 final int x2,
			  				 final int y2 ) 
	  throws JBasicException {
		  throw new NotYetImplementedException();		  
	  }

}
