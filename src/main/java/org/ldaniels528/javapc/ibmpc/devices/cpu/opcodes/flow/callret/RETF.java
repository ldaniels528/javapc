/**
 *
 */
package org.ldaniels528.javapc.ibmpc.devices.cpu.opcodes.flow.callret;

import org.ldaniels528.javapc.ibmpc.devices.cpu.I8086;
import org.ldaniels528.javapc.ibmpc.devices.cpu.opcodes.AbstractOpCode;
import org.ldaniels528.javapc.ibmpc.devices.cpu.opcodes.RegistersAffected;
import org.ldaniels528.javapc.ibmpc.exceptions.X86AssemblyException;
import org.ldaniels528.javapc.ibmpc.system.IbmPcSystem;

/**
 * <pre>
 * Usage:  RET
 * 		   RET     nBytes
 *         RETF
 *         RETF    nBytes
 *  Modifies flags: None
 *
 *  Transfers control from a procedure back to the instruction address
 *  saved on the stack.  "n bytes" is an optional number of bytes to
 *  release.  Far returns pop the IP followed by the CS, while near
 *  returns pop only the IP register.
 * </pre>
 *
 * @author lawrence.daniels@gmail.com
 * @see RET
 * @see RETn
 * @see RETF
 * @see RETFn
 */
@RegistersAffected({"SP"})
public class RETF extends AbstractOpCode {
    private static RETF instance = new RETF();

    /**
     * Private constructor
     */
    protected RETF() {
        super();
    }

    /**
     * @return the singleton instance of this class
     */
    public static RETF getInstance() {
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(IbmPcSystem system, final I8086 cpu) throws X86AssemblyException {
        cpu.returnFar(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConditional() {
        return true;
    }

}