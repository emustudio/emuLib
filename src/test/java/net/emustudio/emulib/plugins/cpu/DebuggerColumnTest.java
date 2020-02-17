/*
 * Run-time library for emuStudio and plugins.
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

package net.emustudio.emulib.plugins.cpu;

import net.emustudio.emulib.runtime.interaction.debugger.DebuggerColumn;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DebuggerColumnTest {

    @Test
    public void testGetDefaultWidthReturnsMinusOne() throws Exception {
        assertEquals(-1, new DebuggerColumnStub().getDefaultWidth());
    }

    private final class DebuggerColumnStub implements DebuggerColumn {

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
