/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.ui.debugger;

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
