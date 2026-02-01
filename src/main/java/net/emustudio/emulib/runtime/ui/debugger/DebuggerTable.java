/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.ui.debugger;

import net.emustudio.emulib.plugins.Plugin;

import java.util.List;

/**
 * Debugger table in emuStudio.
 * <p>
 * Allows to set up custom columns by a plugin. If no plugin sets the columns, emuStudio will provide default columns.
 */
@SuppressWarnings("unused")
public interface DebuggerTable {

    /**
     * Set custom columns in the debugger table.
     * <p>
     * The call has immediate effect. Subsequent calls are allowed; plugins can change the columns at any time. However,
     * it is not recommended to change it once the columns are set.
     * <p>
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
