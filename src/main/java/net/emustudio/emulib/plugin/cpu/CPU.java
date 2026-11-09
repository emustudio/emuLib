/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugin.cpu;

import net.emustudio.emulib.plugin.Plugin;

import net.emustudio.emulib.application.ui.debugger.DebuggerColumn;

import javax.swing.JPanel;
import java.util.List;

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

    /** Executes one instruction synchronously on the host's CPU worker and returns its result. */
    RunState step() throws Exception;

    /**
     * Runs the instruction loop synchronously until natural completion or cooperative interruption.
     * The host supplies the worker thread, serializes CPU mutations, and publishes lifecycle events.
     * Return STATE_RUNNING when interrupted without another terminal reason; the host applies the requested
     * pause/stop state. Natural halt, breakpoint, and failures must retain their actual result.
     */
    RunState execute() throws Exception;

    /** Optional host-scheduled frequency observation. Must not execute instructions or create threads. */
    default float sampleFrequency() { return Float.NaN; }

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
     * Resets the CPU synchronously after the host has acknowledged actual execution exit.
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
     * Get the number of addressable instruction locations exposed by this CPU.
     *
     * @return positive address-space size; the highest valid location is one less than this value
     */
    int getAddressSpaceSize();

    /**
     * Get disassembler.
     * <p>
     * EmuStudio uses it for filling up the debugger table.
     *
     * @return disassembler of CPU instructions
     */
    Disassembler getDisassembler();

    /**
     * Get custom columns of the debugger table.
     *
     * @return debugger columns; empty list means default columns (address, mnemo, opcode)
     */
    default List<DebuggerColumn<?>> getDebuggerColumns() {
        return List.of();
    }

    @Override
    default boolean isAutomationSupported() {
        return true;
    }
}
