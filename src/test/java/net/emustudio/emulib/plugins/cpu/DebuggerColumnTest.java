/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.cpu;

import net.emustudio.emulib.runtime.ui.debugger.DebuggerColumn;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DebuggerColumnTest {

    @Test
    public void testGetDefaultWidthReturnsMinusOne() {
        assertEquals(-1, new DebuggerColumnStub().getDefaultWidth());
    }

    private static final class DebuggerColumnStub implements DebuggerColumn {

        @Override
        public Class getClassType() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getTitle() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isEditable() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setValue(int location, Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object getValue(int location) {
            throw new UnsupportedOperationException();
        }
    }
}
