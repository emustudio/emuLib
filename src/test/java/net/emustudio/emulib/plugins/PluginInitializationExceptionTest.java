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
package net.emustudio.emulib.plugins;

import net.emustudio.emulib.runtime.exceptions.PluginInitializationException;
import org.junit.Test;

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertSame;

public class PluginInitializationExceptionTest {

    @Test(expected = NullPointerException.class)
    public void testNullPluginThrows() throws Exception {
        new PluginInitializationException((Plugin)null);
    }

    @Test(expected = NullPointerException.class)
    public void testMessageNullPluginThrows() throws Exception {
        new PluginInitializationException(null, "message");
    }

    @Test
    public void testNullMessageValidPluginDoesNotThrow() throws Exception {
        new PluginInitializationException(createMock(Plugin.class), (String)null);
    }

    @Test(expected = NullPointerException.class)
    public void testMessageThrowableNullPluginThrows() throws Exception {
        new PluginInitializationException(null, "message", new Exception());
    }

    @Test(expected = NullPointerException.class)
    public void testThrowableNullPluginThrows() throws Exception {
        new PluginInitializationException((Plugin)null, new Exception());
    }

    @Test
    public void testGetPluginWorks() throws Exception {
        Plugin plugin = createMock(Plugin.class);
        PluginInitializationException exception = new PluginInitializationException(plugin);

        assertSame(plugin, exception.getPlugin().get());
    }
}
