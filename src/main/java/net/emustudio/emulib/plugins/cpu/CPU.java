/*
 * This file is part of emuLib.
 *
 * Copyright (C) 2006-2023  Peter Jakubčo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.emustudio.emulib.plugins.cpu;

import net.emustudio.emulib.plugins.Plugin;

import javax.swing.JPanel;

/**
 * CPU plugin root interface.
 * <p>
 * Should be implemented by a plugin. There should exist just one implementation.
 * <p>
 * CPU can define one or more "CPU contexts" (usually just one), which can extend the runtime functionality accessible
 * to plugins.
 *
 * @see CPUContext
 */
@SuppressWarnings("unused")
public interface CPU extends Plugin {

    /**
     * The run state of the CPU.
     */
    enum RunState {
        /**
         * CPU is stopped (naturally or by user) and should not be run until it is reset.
         */
        STATE_STOPPED_NORMAL("stopped"),
        /**
         * CPU is in breakpoint state (paused).
         */
        STATE_STOPPED_BREAK("breakpoint"),
        /**
         * CPU is stopped because of address fallout error. It should not be run until it is reset.
         */
        STATE_STOPPED_ADDR_FALLOUT("stopped (address fallout)"),
        /**
         * CPU is stopped because of instruction fallout (unknown instruction) error. It should not be run until it
         * is reset.
         */
        STATE_STOPPED_BAD_INSTR("stopped (instruction fallout)"),
        /**
         * CPU is running.
         */
        STATE_RUNNING("running");

        private final String name;

        RunState(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }

    }

    /**
     * Interface for receiving CPU events. The class which is interested in processing CPU events implements this
     * interface, and the object created with that class is registered with a CPU, using the CPU's addCPUListener method.
     * When the CPU event occurs, that:
     * <ul>
     *     <li>if the event is CPU's state change, then object's <code>internalStateChanged()</code>
     *         method is invoked.</li>
     *     <li>if the event is CPU's run state change, then object's <code>runStateChanged()</code>
     *         method is invoked.</li>
     * </ul>
     * <p>
     * CONTRACT: Never call further actions on CPU (start,stop,step,reset) from the same thread as from which these
     * methods are called.
     */
    @SuppressWarnings("unused")
    interface CPUListener {

        /**
         * Invoked when an CPU's run state changes.
         *
         * @param runState new run state of the CPU
         */
        void runStateChanged(RunState runState);

        /**
         * Invoked when an CPU's internal state changes. The state can be register change,
         * flags change, or other CPU's internal state change.
         */
        void internalStateChanged();

    }

    /**
     * Adds the specified CPU listener to receive CPU events from this CPU.
     * <p>
     * CPU events occur when CPU changes its state, or run state. CPU state
     * events don't occur if CPU is running, only happens with run state changes.
     * If listener is <code>null</code>, no exception is thrown and no action is
     * performed.
     *
     * @param listener the CPU listener
     */
    void addCPUListener(CPUListener listener);

    /**
     * Removes the specified CPU listener so that it no longer receives CPU
     * events from this CPU.
     * <p>
     * CPU events occur when CPU changes its state, or run
     * state. CPU state events don't occur if CPU is running, only happens with
     * run state changes. If listener is <code>null</code>, no exception is
     * thrown and no action is performed.
     *
     * @param listener the CPU listener to be removed
     */
    void removeCPUListener(CPUListener listener);

    /**
     * Perform one step of CPU emulation. It means that one instruction should
     * be executed. CPU state changes to state "running", then it executes one
     * instruction, and then it should return to state "breakpoint" or "stopped".
     * Correct timing of executed instruction isn't so important.
     */
    void step();

    /**
     * Runs CPU emulation. Change state of CPU to "running" and start
     * instruction fetch/decode/execute loop.
     * <p>
     * The emuStudio creates separate thread for this purpose.
     * <p>
     * While CPU is running, the emuStudio will not allow to call method
     * <code>step()</code>.
     * <p>
     * A good CPU should perform right timing for instructions here.
     * Debug window should not be updated after each instruction execution,
     * in order to the execution loop would be faster.
     */
    void execute();

    /**
     * Pauses the CPU emulation. If a thread was used for CPU execution
     * and is running, then it should be stopped (destroyed) but the CPU state
     * has to be saved for future run. CPU changes it state to "breakpoint".
     */
    void pause();

    /**
     * Stops the CPU emulation. If a thread was used for CPU execution
     * and is running, then it should be stopped (destroyed) but the CPU state
     * can be saved. CPU changes its state to "stopped" and main module should
     * now forbid execution any of methods <code>step()</code>, <code>pause()</code>,
     * <code>execute()</code> until user resets the CPU. Debug window in main
     * module should be updated with saved CPU state.
     */
    void stop();

    /**
     * Get CPU status panel.
     * <p>
     * Each CPU plugin must have a status panel that shows some important CPU status (e.g. registers, flags,
     * run state, etc.) and allows to manage some settings (e.g. a runtime frequency, etc.).
     * <p>
     * This panel is located on the right side in the "emulation" panel of the main module.
     * <p>
     * CPU itself should take care about updating the panel when it is appropriate.
     *
     * @return CPU status panel
     */
    @SuppressWarnings("unused")
    JPanel getStatusPanel();

    /**
     * Determine whether breakpoints are supported by CPU.
     *
     * @return true if breakpoints are supported, false otherwise
     */
    boolean isBreakpointSupported();

    /**
     * Set a breakpoint at a memory location.
     * <p>
     * Does nothing if breakpoints are not supported.
     *
     * @param location memory location where the breakpoint will be set
     * @throws IndexOutOfBoundsException if the memLocation is out of bounds
     * @see CPU#isBreakpointSupported
     */
    void setBreakpoint(int location);

    /**
     * Unset a breakpoint at a memory location.
     * <p>
     * Does nothing if breakpoints are not supported.
     *
     * @param location memory location from where the breakpoint will be unset
     * @throws IndexOutOfBoundsException if the memLocation is out of bounds
     * @see CPU#isBreakpointSupported
     */
    void unsetBreakpoint(int location);

    /**
     * Determine if a breakpoint is set at a memory location.
     *
     * @param location memory location, from where the breakpoint will be determined
     * @return true if breakpoint is set in the location, false otherwise or if breakpoints are not supported.
     * @throws IndexOutOfBoundsException if the memLocation is out of bounds
     * @see CPU#isBreakpointSupported
     */
    boolean isBreakpointSet(int location);

    /**
     * Resets the CPU and sets instruction position to given program location.
     *
     * @param location Program location in memory
     */
    void reset(int location);

    /**
     * Get memory location of an instruction which will be emulated as next.
     * <p>
     * Instruction position is more "abstract" name for "program counter", which is mostly used in 8-bit architectures.
     *
     * @return memory location of an instruction which will be emulated as next
     */
    int getInstructionLocation();

    /**
     * Set memory location of an instruction which will be emulated as next.
     * <p>
     * Instruction position is more "abstract" name for "program counter", which is mostly used in 8-bit architectures.
     * <p>
     * This method is called by emuStudio application when a user perform "jump to address" operation.
     *
     * @param location new address of actual instruction
     * @return true if operation was successful, false otherwise
     */
    boolean setInstructionLocation(int location);

    /**
     * Get disassembler.
     * <p>
     * EmuStudio uses it for filling up the debugger table.
     *
     * @return disassembler of CPU instructions
     */
    Disassembler getDisassembler();

    @Override
    default boolean isAutomationSupported() {
        return true;
    }
}
