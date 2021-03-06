package org.ldaniels528.javapc.ibmpc.devices.cpu.opcodes.flow.jump;

import org.ldaniels528.javapc.ibmpc.devices.cpu.I8086;
import org.ldaniels528.javapc.ibmpc.devices.cpu.operands.Operand;
import org.ldaniels528.javapc.ibmpc.devices.cpu.opcodes.flow.AbstractFlowControlOpCode;

/**
 * <pre>
 * Jump if Zero
 * Jump Condition: ZF=1
 * </pre>
 *
 * @author lawrence.daniels@gmail.com
 */
public class JZ extends AbstractFlowControlOpCode {

    /**
     * Creates a new conditional jump instruction
     *
     * @param destination the given memory offset to jump to.
     */
    public JZ(final Operand destination) {
        super(destination);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean redirectsFlow(final I8086 cpu) {
        return cpu.FLAGS.isZF();
    }

}