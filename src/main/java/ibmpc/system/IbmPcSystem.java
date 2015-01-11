package ibmpc.system;

import ibmpc.devices.cpu.Intel80x86;
import ibmpc.devices.cpu.OpCode;
import ibmpc.devices.cpu.x86.bios.IbmPcBIOS;
import ibmpc.devices.display.IbmPcDisplay;
import ibmpc.devices.keyboard.IbmPcKeyboard;
import ibmpc.devices.memory.IbmPcRandomAccessMemory;
import ibmpc.devices.mouse.IbmPcMouse;
import ibmpc.devices.ports.IbmPcHardwarePorts;
import ibmpc.devices.storage.IbmPcStorageSystem;
import ibmpc.exceptions.X86AssemblyException;

import java.util.List;

/**
 * Represents an IBM PC/XT/AT Compatible System
 *
 * @author lawrence.daniels@gmail.com
 */
public interface IbmPcSystem {

    /**
     * Executes the given collection of opCodes
     *
     * @param opCodes the given collection of {@link OpCode opCodes}
     * @throws X86AssemblyException
     */
    void execute(List<OpCode> opCodes) throws X86AssemblyException;

    /**
     * @return the Basic Input/Output System (BIOS)
     */
    IbmPcBIOS getBIOS();

    /**
     * @return the IBM PC-compatible central processing unit (CPU)
     */
    Intel80x86 getCPU();

    /**
     * @return the graphical display manager.
     */
    IbmPcDisplay getDisplay();

    /**
     * @return the hardware ports
     */
    IbmPcHardwarePorts getHardwarePorts();

    /**
     * @return the console input device associated to this environment.
     */
    IbmPcKeyboard getKeyboard();

    /**
     * @return the mouse input device associated to this environment.
     */
    IbmPcMouse getMouse();

    /**
     * @return the random access memory instance of this environment.
     */
    IbmPcRandomAccessMemory getRandomAccessMemory();

    /**
     * @return the persistence storage device belonging to this environment.
     */
    IbmPcStorageSystem getStorageSystem();

    /**
     * @return detail information about the system
     */
    IbmPcSystemInfo getInformation();

}
