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
package net.emustudio.emulib.runtime;

import net.emustudio.emulib.runtime.interaction.Dialogs;
import net.emustudio.emulib.runtime.interaction.debugger.DebuggerTable;
import net.jcip.annotations.ThreadSafe;

/**
 * emuStudio application API.
 * <p>
 * Plugins can use it for the communication with emuStudio.
 */
@SuppressWarnings("unused")
@ThreadSafe
public interface ApplicationApi {

    /**
     * Get debugger table.
     *
     * @return debugger table in emuStudio.
     */
    DebuggerTable getDebuggerTable();

    /**
     * Get context pool.
     * <p>
     * Context pool is used for registration and obtaining plugin contexts, which are used in plugin communication.
     *
     * @return context pool
     */
    ContextPool getContextPool();

    /**
     * Get dialogs object.
     * <p>
     * Dialogs object can be used by plugins to display common dialogs like information/error messages, or ask user
     * for input, like read integer/double value or ask for confirmation.
     *
     * @return dialogs object
     */
    Dialogs getDialogs();

    /**
     * Sets program start location (usually a memory address).
     * <p>
     * This method is usually called by a compiler when it finishes compilation process, if it's clear at
     * which memory location the compiled program starts. The start location is then used by CPU, in a reset
     * operation - PC (program counter, or something similar) is usually set to this location.
     *
     * @param location program start location (usually a memory address)
     */
    void setProgramLocation(int location);

    /**
     * Gets program start location (usually a memory address).
     * <p>
     * This method is usually called by a CPU in a reset operation - PC (program counter, or something similar) is
     * usually set to this location.
     *
     * @return program's start address in memory
     */
    int getProgramLocation();

    /**
     * "Unavailable" instance of ApplicationApi.
     * <p>
     * It means that all methods return dummy or null values.
     * <p>
     * The instance might be useful when creating plugin object without emuStudio (e.g. a plugin wants to support
     * command-line interface).
     */
    ApplicationApi UNAVAILABLE = new ApplicationApi() {
        /**
         * Returns null.
         * @return null
         */
        @Override
        public DebuggerTable getDebuggerTable() {
            return null;
        }

        /**
         * Returns null.
         * @return null
         */
        @Override
        public ContextPool getContextPool() {
            return null;
        }

        /**
         * Returns null.
         * @return null
         */
        @Override
        public Dialogs getDialogs() {
            return null;
        }

        /**
         * Does nothing.
         * @param location program start location (usually a memory address)
         */
        @Override
        public void setProgramLocation(int location) {

        }

        /**
         * Returns 0
         * @return 0
         */
        @Override
        public int getProgramLocation() {
            return 0;
        }
    };
}
