package org.ldaniels528.javapc.ibmpc.devices.cpu.opcodes.flags;

import org.ldaniels528.javapc.ibmpc.devices.cpu.I8086;
import org.ldaniels528.javapc.ibmpc.devices.cpu.opcodes.AbstractOpCode;
import org.ldaniels528.javapc.ibmpc.devices.cpu.opcodes.FlagsAffected;
import org.ldaniels528.javapc.ibmpc.devices.cpu.opcodes.RegistersAffected;
import org.ldaniels528.javapc.ibmpc.system.IbmPcSystem;

/**
 * <pre>
 * Load Register AH From Flags
 *
 * Usage: LAHF
 *
 * Modifies Flags: None
 *
 * Copies bits 0-7 of the flags register into AH.
 * This includes flags AF, CF, PF, SF and ZF other bits
 * are undefined.
 *
 * AH := SF ZF xx AF xx PF xx CF
 * </pre>
 *
 * @author lawrence.daniels@gmail.com
 */
@FlagsAffected({"AF", "CF", "PF", "SF", "ZF"})
@RegistersAffected({"AH"})
public class LAHF extends AbstractOpCode {
    private static final LAHF instance = new LAHF();

    /**
     * Private constructor
     */
    private LAHF() {
        super();
    }

    /**
     * @return the singleton instance of this class
     */
    public static LAHF getInstance() {
        return instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(IbmPcSystem system, final I8086 cpu) {
        cpu.AH.set(cpu.FLAGS.get() & 0b0111_1111);
    }

}