/**
 * ICPU.java
 * 
 * (c) Copyright 2008-2010, P.Jakubčo <pjakubco@gmail.com>
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
package plugins.cpu;

import javax.swing.JPanel; 
import plugins.memory.IMemoryContext;
import plugins.IPlugin; 
import plugins.ISettingsHandler; 

/**
 * Interface that covers CPU. This is the main interface that CPU plugin
 * has to implement.
 */
public interface ICPU extends IPlugin {

    /**
     * CPU is stopped (naturally or by user) and should not be run until its
     * reset.
     */
    public static final int STATE_STOPPED_NORMAL       = 1;

    /**
     * CPU is in breakpoint state (paused).
     */
    public static final int STATE_STOPPED_BREAK        = 2;

    /**
     * CPU is stopped because of address fallout error. It should not be
     * run until its reset.
     */
    public static final int STATE_STOPPED_ADDR_FALLOUT = 3;

    /**
     * CPU is stopped because of instruction fallout (unknown instruction) 
     * error. It should not be run until its reset.
     */
    public static final int STATE_STOPPED_BAD_INSTR    = 4;

    /**
     * CPU is running.
     */
    public static final int STATE_RUNNING              = 5;

    /**
     * Perform initialization of CPU. This method is called after compiler
     * successful initialization. Initialization process of CPU can be
     * various, e.g. check for memory type, retrieve some settings from
     * configuration file, etc.
     * @param mem       memory context that this CPU should use. If CPU and memory
     *                  aren't connected in abstract scheme, this will be
     *                  <code>null</code>. Plugin should therefore check this
     *                  variable and in the case of null memory and if CPU can't
     *                  work without memory, plugin should display error message
     *                  and then return false.
     * @param sHandler  settings handler object. CPU can use this for
     *                  accessing/storing/removing its settings.
     * @return true if initialization was successful, false otherwise
     */
    public boolean initialize (IMemoryContext mem, ISettingsHandler sHandler);

    /**
     * Perform one step of CPU emulation. It means that one instruction should
     * be executed. CPU state changes to state "running", then it executes one
     * instruction, and then it should return to state "breakpoint" or "stopped".
     * Correct timing of executed instruction isn't so important.
     */
    public void step ();

    /**
     * Runs CPU emulation. Change state of CPU to "running" and start
     * instruction fetch/decode/execute loop. Best for this purpose is to create
     * a separate thread that runs permanently and protect emulator from "freeze",
     * the cause of started instruction execution loop. It should be possible
     * to control the thread in future by other methods: <code>pause()</code>
     * and <code>stop()</code>. Therefore after CPU's run the execution should
     * return from this method. It can be assumed that while CPU is in run state,
     * main module won't allow to call method <code>step()</code>.
     * 
     * In CPU run state (a good CPU) performs right timing for instructions.
     * Debug window isn't updated after execution of each instruction, so
     * execution loop should be faster as it is by calling <code>step</code>
     * method.
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
     * Get CPU context. CPU context is an object that implements basic
     * <code>ICPUContext</code> interface. Often this interface is extended
     * by another (not supported by this library), a concrete context for
     * concrete CPU and gives more functionality than basic one. Context is
     * a place, where plugin programmer should implement unsupported, but needed
     * methods and then he should make public its interface. Plugins connected 
     * to CPU get its context as a parameter in plugin connection process, so
     * they can (and should do it in that way) identify the (CPU context)
     * interface and other context information, such as ID or version. After
     * this recognize process plugins recognize (or do not recognize)
     * supported CPU they can be connected with.
     * @return CPU context object
     */
    public ICPUContext getContext ();

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
    public IDebugColumn[] getDebugColumns ();

    /**
     * Called when user sets a value to a cell in debug window. This method
     * should ensure proper changes in CPU's internal state, caused by this
     * set. The main module calls this method only if the cell in debug window
     * is editable by user (<code>IDebugColumn.isEditable()</code>).
     * @param row    cell's index from memory position 0 (not row in debug table)
     * @param col    column of the cell in debug window table
     * @param value  new value of the cell
     */
    public void setDebugValue (int row, int col, Object value);

    /**
     * Gets the value of a cell in debug window on specified position. 
     * @param row  cell's index from memory position 0 (not row in debug table)
     * @param col  column of the cell in in debug window table
     * @return value of the cell
     */
    public Object getDebugValue (int row, int col);

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
     * Method compute address of an instruction that follows after instruction
     * defined by given address. Main module uses this method to determine
     * on which address should start next instruction in debug window. Several
     * calls of this method make possible to create a list of instructions that
     * begin on arbitrary address (debug window table).
     * 
     * @param pos  memory position (address) of an instruction
     * @return address of an instruction followed by specified address
     */
    public int getInstrPosition (int pos);

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

}

