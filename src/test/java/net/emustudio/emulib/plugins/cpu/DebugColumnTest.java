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

package net.emustudio.emulib.plugins.cpu;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DebugColumnTest {

    @Test
    public void testGetDefaultWidthReturnsMinusOne() throws Exception {
        assertEquals(-1, new DebugColumnStub().getDefaultWidth());
    }

    private final class DebugColumnStub implements DebugColumn {

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
        public void setDebugValue(int location, Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object getDebugValue(int location) {
            throw new UnsupportedOperationException();
        }
    }
}
