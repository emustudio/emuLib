// SPDX-License-Identifier: GPL-3.0-or-later
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
    };
}
