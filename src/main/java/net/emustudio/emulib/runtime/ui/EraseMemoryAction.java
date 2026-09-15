/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.ui;

import net.emustudio.emulib.plugins.memory.MemoryContext;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.event.ActionEvent;
import java.util.Objects;

/**
 * Reusable Swing action which erases (clears) a memory context and refreshes its table model.
 * <p>
 * Memory plugins differ only in the concrete memory-context and table-model types and in the icon, so the
 * behavior itself is shared here to avoid per-plugin copies.
 */
public class EraseMemoryAction extends AbstractAction {
    private final AbstractTableModel tableModel;
    private final MemoryContext<?> context;

    /**
     * Creates the erase-memory action.
     *
     * @param tableModel the memory table model to refresh after erasing
     * @param context    the memory context to clear
     * @param icon       the action icon (may be {@code null})
     */
    public EraseMemoryAction(AbstractTableModel tableModel, MemoryContext<?> context, Icon icon) {
        super("Erase memory", icon);
        this.tableModel = Objects.requireNonNull(tableModel);
        this.context = Objects.requireNonNull(context);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        context.clear();
        tableModel.fireTableDataChanged();
    }
}
