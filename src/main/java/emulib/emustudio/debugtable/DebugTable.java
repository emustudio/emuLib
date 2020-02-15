/*
 * Run-time library for emuStudio and plug-ins.
 *
 *     Copyright (C) 2006-2020  Peter Jakubčo
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package emulib.emustudio.debugtable;

import emulib.plugins.cpu.DebugColumn;
import java.util.List;

/**
 * This interface allows updating Debug table in emuStudio by any plug-in.
 */
public interface DebugTable {

    /**
     * Redraws debug table according to actual values.
     */
    void refresh();

    /**
     * Set custom columns in debugger.
     *
     * @param customColumns non-null debug columns
     * @see AddressColumn
     * @see BreakpointColumn
     * @see MnemoColumn
     * @see OpcodeColumn
     */
    void setCustomColumns(List<DebugColumn> customColumns);

    /**
     * Set default columns in debugger.
     *
     * Default columns is specific for emuStudio. Usually, there is breakpoint column, address, mnemo and opcode.
     */
    void setDefaultColumns();

}
