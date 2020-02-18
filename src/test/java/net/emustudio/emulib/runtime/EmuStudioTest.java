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
package net.emustudio.emulib.runtime;

import net.emustudio.emulib.runtime.interaction.debugger.DebuggerTable;
import org.junit.Test;

import static org.easymock.EasyMock.createNiceMock;
import static org.junit.Assert.*;

public class EmuStudioTest {
    private final String token = "token";

    @Test
    public void testDebugTableIsSet() throws InvalidTokenException {
        EmuStudio emuStudio = new EmuStudio(token);

        DebuggerTable debug = createNiceMock(DebuggerTable.class);

        emuStudio.setDebugTable(debug, token);

        assertTrue(emuStudio.getDebuggerTable().isPresent());
        assertSame(emuStudio.getDebuggerTable().get(), debug);
    }

    @Test(expected = InvalidTokenException.class)
    public void testVerifierInvalidToken() throws InvalidTokenException {
        EmuStudio emuStudio = new EmuStudio(token);

        DebuggerTable debug = createNiceMock(DebuggerTable.class);
        emuStudio.setDebugTable(debug,token + "definitely invalid");
    }

    @Test(expected = NullPointerException.class)
    public void testNullTokenIsNotAllowed() throws NullPointerException {
        new EmuStudio(null);
    }
}
