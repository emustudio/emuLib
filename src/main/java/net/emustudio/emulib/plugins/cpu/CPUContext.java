/*
 * This file is part of emuLib.
 *
 * Copyright (C) 2006-2020  Peter Jakubčo
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

import net.emustudio.emulib.plugins.annotations.PluginContext;
import net.emustudio.emulib.plugins.Context;

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
     * Determines if run callbacks are supported.
     *
     * @return true if run callbacks are supported; false otherwise.
     */
    default boolean isRunCallbackSupported() {
        return false;
    }

    /**
     * Allows to register a run callback to CPU, which will be triggered after each instruction execution.
     * It is up to a CPU if this is actually implemented.
     * <p>
     * The callbacks are triggered on the same thread as instruction execution.
     *
     * @param runnable runnable
     */
    default void registerRunCallback(Runnable runnable) {

    }

    /**
     * Unregisters a run callback from this CPU.
     *
     * @param runnable runnable
     */
    default void unregisterRunCallback(Runnable runnable) {

    }
}

