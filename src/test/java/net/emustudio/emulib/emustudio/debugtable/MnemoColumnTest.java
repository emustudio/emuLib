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
package net.emustudio.emulib.emustudio.debugtable;

import net.emustudio.emulib.plugins.cpu.DisassembledInstruction;
import net.emustudio.emulib.plugins.cpu.Disassembler;
import net.emustudio.emulib.runtime.exceptions.InvalidInstructionException;
import org.junit.Test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

public class MnemoColumnTest {

    @Test(expected = NullPointerException.class)
    public void testNullDisassemblerThrows() throws Exception {
        new MnemoColumn(null);
    }

    @Test
    public void testClassTypeIsString() throws Exception {
        MnemoColumn column = new MnemoColumn(createMock(Disassembler.class));

        assertEquals(String.class, column.getClassType());
    }

    @Test
    public void testMnemoColumnTitleIsCorrect() throws Exception {
        MnemoColumn column = new MnemoColumn(createMock(Disassembler.class));

        assertEquals("mnemonics", column.getTitle());
    }

    @Test
    public void testDisassembleInvalidInstruction() throws Exception {
        Disassembler disassembler = createMock(Disassembler.class);
        expect(disassembler.disassemble(0x53)).andThrow(new InvalidInstructionException()).anyTimes();
        replay(disassembler);

        MnemoColumn column = new MnemoColumn(disassembler);

        assertEquals("[invalid]", column.getDebugValue(0x53));

        verify(disassembler);
    }

    @Test
    public void testDisassembleIncompleteInstruction() throws Exception {
        Disassembler disassembler = createMock(Disassembler.class);
        expect(disassembler.disassemble(0x53)).andThrow(new IndexOutOfBoundsException()).anyTimes();
        replay(disassembler);

        MnemoColumn column = new MnemoColumn(disassembler);

        assertEquals("[incomplete]", column.getDebugValue(0x53));

        verify(disassembler);
    }

    @Test
    public void testMnemoIsReturned() throws Exception {
        DisassembledInstruction instruction = createMock(DisassembledInstruction.class);
        expect(instruction.getMnemo()).andReturn("some mnemo").once();
        replay(instruction);

        Disassembler disassembler = createMock(Disassembler.class);
        expect(disassembler.disassemble(0x53)).andReturn(instruction).once();
        replay(disassembler);

        MnemoColumn column = new MnemoColumn(disassembler);

        assertEquals("some mnemo", column.getDebugValue(0x53));

        verify(instruction, disassembler);
    }

    @Test
    public void testDefaultWidthIsNotPredefined() throws Exception {
        assertEquals(-1, new MnemoColumn(createMock(Disassembler.class)).getDefaultWidth());
    }

    @Test
    public void testSetDebugValueJustForIncreasingCoverage() throws Exception {
        new MnemoColumn(createMock(Disassembler.class)).setDebugValue(-1, null);
    }
}
