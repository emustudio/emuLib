// SPDX-License-Identifier: GPL-3.0-or-later
package net.emustudio.emulib.runtime.interaction.debugger;

/**
 * Thrown if a value cannot be set to the cell in the debugger table.
 */
@SuppressWarnings("unused")
public class CannotSetDebuggerValueException extends Exception {
    CannotSetDebuggerValueException(String message) {
        super(message);
    }

    CannotSetDebuggerValueException(String message, Throwable cause) {
        super(message, cause);
    }
}
