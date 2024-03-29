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
