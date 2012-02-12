/**
 * ICPU.java
 * 
 * (c) Copyright 2008-2011, P.Jakubčo <pjakubco@gmail.com>
 * 
 * KISS, YAGNI
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package emulib.plugins.cpu;

import java.util.EventListener;
import java.util.EventObject;
import javax.swing.JPanel; 
import emulib.plugins.IPlugin;

/**
 * Interface that covers CPU. This is the main interface that CPU plugin
 * has to implement.
 */
public interface ICPU extends IPlugin {

    /**
     * The run state of the CPU.
     */
    public enum RunState {
        /**
         * CPU is stopped (naturally or by user) and should not be run until its
         * reset.
         */
        STATE_STOPPED_NORMAL,
        /**
         * CPU is in breakpoint state (paused).
         */
        STATE_STOPPED_BREAK,
        /**
         * CPU is stopped because of address fallout error. It should not be
         * run until its reset.
         */
        STATE_STOPPED_ADDR_FALLOUT,
        /**
         * CPU is stopped because of instruction fallout (unknown instruction)
         * error. It should not be run until its reset.
         */
        STATE_STOPPED_BAD_INSTR,
        /**
         * CPU is running.
         */
        STATE_RUNNING
    }

    /**
     * Adds the specified CPU listener to receive CPU events from this CPU.
     * CPU events occur when CPU changes its state, or run state. CPU state
     * events don't occur if CPU is running, only happens with run state changes.
     * If listener is <code>null</code>, no exception is thrown and no action is
     * performed.
     * @param listener  the CPU listener
     */
    public void addCPUListener (ICPUListener listener);

    /**
     * Removes the specified CPU listener so that it no longer receives CPU
     * events from this CPU. CPU events occur when CPU changes its state, or run
     * state. CPU state events don't occur if CPU is running, only happens with
     * run state changes. If listener is <code>null</code>, no exception is
     * thrown and no action is performed.
     * @param listener  the CPU listener to be removed
     */
    public void removeCPUListener (ICPUListener listener);

    /**
     * The listener interface for receiving CPU events. The class that is
     * interested in processing a CPU event implements this interface, and the
     * object created with that class is registered with a CPU, using the CPU's
     * addCPUListener method.
     * When the CPU event occurs, that:
     * <ul>
     *     <li>if the event is CPU's state change, then object's <code>stateUpdated()</code>
     *         method is invoked.</li>
     *     <li>if the event is CPU's run state change, then object's <code>runChanged()</code>
     *         method is invoked.</li>
     * </ul>
     */
    public interface ICPUListener extends EventListener {

        /**
         * Invoked when an CPU's run state changes.
         * @param evt       event object
         * @param runState  new run state of the CPU
         */
        public void runChanged (EventObject evt, RunState runState);

        /**
         * Invoked when an CPU's state changes. The state can be register change,
         * flags change, or other CPU's internal state change.
         * @param evt  event object
         */
        public void stateUpdated (EventObject evt);

    }

    /**
     * Perform one step of CPU emulation. It means that one instruction should
     * be executed. CPU state changes to state "running", then it executes one
     * instruction, and then it should return to state "breakpoint" or "stopped".
     * Correct timing of executed instruction isn't so important.
     */
    public void step ();

    /**
     * Runs CPU emulation. Change state of CPU to "running" and start
     * instruction fetch/decode/execute loop.
     *
     * The emuStudio creates separate thread for this purpose.
     *
     * While CPU is running, the emuStudio will not allow to call method
     * <code>step()</code>.
     * 
     * A good CPU should performs right timing for instructions here.
     * Debug window should not be updated after each instruction execution,
     * in order to the execution loop would be faster.
     */
    public void execute ();

    /**
     * Pauses the CPU emulation. If a thread was used for CPU execution
     * and is running, then it should be stopped (destroyed) but the CPU state
     * has to be saved for future run. CPU changes it state to "breakpoint".
     */
    public void pause ();

    /**
     * Stops the CPU emulation. If a thread was used for CPU execution
     * and is running, then it should be stopped (destroyed) but the CPU state
     * can be saved. CPU changes its state to "stopped" and main module should
     * now forbid execution any of methods <code>step()</code>, <code>pause()</code>,
     * <code>execute()</code> until user resets the CPU. Debug window in main
     * module should be updated with saved CPU state.
     */
    public void stop ();

    /**
     * Gets CPU GUI panel. Each CPU plugin should have GUI panel that shows
     * some important CPU status (e.g. registers, flags, run state, etc.) and
     * allow to user perform some settings (e.g. set the frequency, etc.). This
     * panel is located on right side in panel "emulation" in main module. CPU
     * plugin should update the panel immediately after CPU state changes somehow.
     * @return status GUI panel (instance object)
     */
    public JPanel getStatusGUI ();

    /**
     * Gets columns in debug window. These columns will be used in the list
     * in the debug window. Usually CPU uses these columns: "breakpoint",
     * "address", "mnemonics" and "opcode". 
     * @return debug columns array
     */
//    public IDebugColumn[] getDebugColumns ();

    /**
     * Determine whether breakpoints are supported by CPU.
     * @return true if breakpoints are supported, false otherwise
     */
    public boolean isBreakpointSupported ();

    /**
     * Set/unset a breakpoint to specified memory position (address). It should
     * be called only if breakpoints are supported
     * (<code>isBreakpointSupported()</code>).
     * @param pos  memory address where breakpoint should be set/unset
     * @param set  true if breakpoint should be set, false otherwise
     */
    public void setBreakpoint (int pos, boolean set);

    /**
     * Determine breakpoint on specified address. It should be called only if
     * breakpoints are supported (<code>isBreakpointSupported()</code>), otherwise
     * the method should return false always.
     * @param pos  memory position (address), from where we try to determine 
     *             breakpoint
     * @return true if breakpoint exists on specified address, false otherwise
     */
    public boolean getBreakpoint (int pos);

    /**
     * Perform reset of the CPU with specific starting address. This is used
     * when program starting address is known. Otherwise it is used standard
     * <code>Plugin.reset()</code> method
     * 
     * @param startAddress
     */
    public void reset(int startAddress);
    
    /**
     * Get actual instruction position (before its execution). Can be said,
     * that this method should return PC (program counter) register (if CPU
     * has one).
     * 
     * @return memory position (address) of next instruction
     */
    public int getInstrPosition ();

    /**
     * Set new actual instruction position (that will be executed as next). It
     * can be said, that a parameter represents new value of PC (program counter),
     * if CPU has one. Otherwise CPU should interpret the position in the right
     * manner. 
     * 
     * This method is called by main module when user perform "jump to address"
     * operation.
     * @param pos  new address of actual instruction
     * @return true if operation was successful, false otherwise
     */
    public boolean setInstrPosition (int pos);

    /**
     * Get disassembler object. EmuStudio uses this for creating debug table.
     *
     * @return disassembler object
     */
    public IDisassembler getDisassembler();
}

