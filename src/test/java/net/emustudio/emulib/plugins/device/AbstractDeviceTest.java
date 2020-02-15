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
package net.emustudio.emulib.plugins.device;

import net.emustudio.emulib.emustudio.SettingsManager;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertEquals;

public class AbstractDeviceTest {
    private AbstractDeviceStub device;

    @Before
    public void setUp() {
        device = new AbstractDeviceStub(0L);
    }

    @Test(expected = NullPointerException.class)
    public void testCreateInstanceWithNullPluginIDThrows() throws Exception {
        new AbstractDeviceStub(null);
    }

    @Test
    public void testInitializeWithCorrectParameterDoesNotThrow() throws Exception {
        device.initialize(createMock(SettingsManager.class));
    }

    @Test
    public void testGetTitle() {
        assertEquals("title", device.getTitle());
    }

    @Test
    public void testResetForIncreasingCoverage() throws Exception {
        device.reset();
    }
}
