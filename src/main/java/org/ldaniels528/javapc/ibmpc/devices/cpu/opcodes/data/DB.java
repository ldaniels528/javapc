package org.ldaniels528.javapc.ibmpc.devices.cpu.opcodes.data;

import org.ldaniels528.javapc.ibmpc.devices.cpu.I8086;
import org.ldaniels528.javapc.ibmpc.devices.cpu.opcodes.AbstractOpCode;
import org.ldaniels528.javapc.ibmpc.exceptions.X86AssemblyException;
import org.ldaniels528.javapc.ibmpc.system.IbmPcSystem;

/**
 * Define Byte (DB) MACRO Instruction
 *
 * @author ldaniels
 */
public class DB extends AbstractOpCode {
    private int value;

    /**
     * Creates a new define byte instruction
     *
     * @param byteValue the given byte value
     */
    public DB(final int byteValue) {
        this.value = byteValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(IbmPcSystem system, final I8086 cpu)
            throws X86AssemblyException {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("DB %02X", value);
    }

}
