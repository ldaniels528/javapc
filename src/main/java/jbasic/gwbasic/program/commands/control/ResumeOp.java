package jbasic.gwbasic.program.commands.control;

import jbasic.common.exceptions.JBasicException;
import jbasic.common.program.JBasicCompiledCode;
import jbasic.common.util.JBasicTokenUtil;
import jbasic.gwbasic.program.commands.GwBasicCommand;

import com.ldaniels528.tokenizer.TokenIterator;

/**
 * RESUME Statement
 * Syntax: RESUME [label/lineNumber]
 */
public class ResumeOp extends GwBasicCommand {
	  private final String label;

	  /**
	   * Creates an instance of this opCode
	   * @param it the parsed text that describes the BASIC instruction
	   * @throws jbasic.common.exceptions.JBasicException
	   */
	  public ResumeOp( TokenIterator it ) throws JBasicException {
		    this.label = JBasicTokenUtil.nextToken( it ); 
		    JBasicTokenUtil.noMoreTokens( it );
	  }

	  /* 
	   * (non-Javadoc)
	   * @see jbasic.program.opcodes.OpCode#execute(jbasic.program.JBasicProgram, jbasic.environment.JBasicEnvironment)
	   */
	  public void execute( final JBasicCompiledCode compiledCode ) 
	  throws JBasicException {
		  // goto the label
		  compiledCode.gotoLabel( label, false );
	  }

}