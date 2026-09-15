/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.ui;

import net.emustudio.emulib.plugins.memory.MemoryContext;
import org.junit.Test;

import javax.swing.table.AbstractTableModel;
import java.awt.event.ActionEvent;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertTrue;

public class EraseMemoryActionTest {

    @Test(expected = NullPointerException.class)
    public void testNullTableModelThrows() {
        new EraseMemoryAction(null, createMock(MemoryContext.class), null);
    }

    @Test(expected = NullPointerException.class)
    public void testNullContextThrows() {
        new EraseMemoryAction(new DummyTableModel(), null, null);
    }

    @Test
    public void testActionPerformedClearsMemoryAndRefreshesTable() {
        MemoryContext<?> context = createMock(MemoryContext.class);
        context.clear();
        expectLastCall().once();
        replay(context);

        AtomicBoolean refreshed = new AtomicBoolean(false);
        AbstractTableModel tableModel = new DummyTableModel();
        tableModel.addTableModelListener(e -> refreshed.set(true));

        new EraseMemoryAction(tableModel, context, null)
                .actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "erase"));

        verify(context);
        assertTrue("expected table model to fire a data-changed event", refreshed.get());
    }

    private static final class DummyTableModel extends AbstractTableModel {
        @Override
        public int getRowCount() {
            return 0;
        }

        @Override
        public int getColumnCount() {
            return 0;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return null;
        }
    }
}
