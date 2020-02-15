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

package net.emustudio.emulib.plugins.memory;

import net.emustudio.emulib.emustudio.SettingsManager;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertEquals;

public class AbstractMemoryTest {
    private AbstractMemoryStub memory;

    @Before
    public void setUp() {
        memory = new AbstractMemoryStub(0L);
    }

    @Test
    public void testInitializeWithCorrectParameterDoesNotThrow() throws Exception {
        memory.initialize(createMock(SettingsManager.class));
    }

    @Test(expected = NullPointerException.class)
    public void testNewInstanceWithNullIdThrows() throws Exception {
        new AbstractMemoryStub(null);
    }

    @Test
    public void testGetTitle() {
        assertEquals("title", memory.getTitle());
    }

    @Test
    public void testProgramStartIsZeroAfterStart() {
        assertEquals(0, memory.getProgramStart());
    }

    @Test
    public void testSetAndGetProgramStart() {
        memory.setProgramStart(555);
        assertEquals(555, memory.getProgramStart());
    }

    @Test
    public void testResetCallForIncreasingCoverage() throws Exception {
        memory.reset();
    }
}
