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

import net.emustudio.emulib.plugins.Plugin;

import java.util.List;

/**
 * Debugger table in emuStudio.
 *
 * Allows to set up custom columns by a plugin. If no plugin sets the columns, emuStudio will provide default columns.
 */
@SuppressWarnings("unused")
public interface DebuggerTable {

    /**
     * Set custom columns in the debugger table.
     *
     * The call has immediate effect. Subsequent calls are allowed; plugins can change the columns at any time. However,
     * it is not recommended to change it once the columns are set.
     *
     * NOTE: Should be called in {@link Plugin#initialize()} method, not sooner.
     *
     * @param columns non-null debug columns
     * @see AddressColumn
     * @see BreakpointColumn
     * @see MnemoColumn
     * @see OpcodeColumn
     */
    void setDebuggerColumns(List<DebuggerColumn<?>> columns);
}
