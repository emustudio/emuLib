/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.cpu;

import net.emustudio.emulib.plugins.annotations.PluginContext;
import net.emustudio.emulib.plugins.Context;

import java.util.Optional;

/**
 * CPU context can be used by plugins which are connected to CPU.
 * <p>
 * Custom CPU contexts can extend the runtime functionality accessible to plugins. Plugins which need the specific
 * CPU contexts, should declare a dependency on the CPU plugin.
 */
@SuppressWarnings("unused")
@PluginContext
public interface CPUContext extends Context {

    /**
     * Determine whether this CPU supports interrupts.
     *
     * @return true, if interrupts are supported, false otherwise
     */
    default boolean isInterruptSupported() {
        return false;
    }

    /**
     * Send interrupt signal to the CPU.
     *
     * @param data data bus
     */
    default void signalInterrupt(byte[] data) {

    }

    /**
     * Get CPU frequency in kHz
     *
     * @return CPU frequency in kHz or 0 if it is not supported
     */
    default int getCPUFrequency() {
        return 0;
    }

    /**
     * Determines if this CPU context supports providing passed cycles
     *
     * @return true if passed cycles is supported by this CPU; false otherwise
     */
    boolean passedCyclesSupported();

    /**
     * Adds passed cycles listener
     *
     * @param passedCyclesListener passed cycles listener
     */
    void addPassedCyclesListener(PassedCyclesListener passedCyclesListener);

    /**
     * Removes given passed cycles listener
     *
     * @param passedCyclesListener passed cycles listener
     */
    void removePassedCyclesListener(PassedCyclesListener passedCyclesListener);

    /**
     * Listener for passed CPU cycles
     */
    interface PassedCyclesListener {

        /**
         * Notifies the context that CPU has executed given number of cycles.
         * Consumer is responsible for handling overflows.
         *
         * @param cyclesDelta cycles that were executed (delta from the last call)
         */
        void passedCycles(long cyclesDelta);
    }
}

