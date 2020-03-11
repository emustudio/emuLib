// SPDX-License-Identifier: GPL-3.0-or-later
package net.emustudio.emulib.runtime.interaction.debugger;

import net.emustudio.emulib.plugins.cpu.DisassembledInstruction;
import net.emustudio.emulib.plugins.cpu.Disassembler;
import net.emustudio.emulib.plugins.cpu.InvalidInstructionException;
import org.junit.Test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

public class MnemoColumnTest {

    @Test(expected = NullPointerException.class)
    public void testNullDisassemblerThrows() {
        new MnemoColumn(null);
    }

    @Test
    public void testClassTypeIsString() {
        MnemoColumn column = new MnemoColumn(createMock(Disassembler.class));

        assertEquals(String.class, column.getClassType());
    }

    @Test
    public void testMnemoColumnTitleIsCorrect() {
        MnemoColumn column = new MnemoColumn(createMock(Disassembler.class));

        assertEquals("instruction", column.getTitle());
    }

    @Test
    public void testDisassembleInvalidInstruction() throws Exception {
        Disassembler disassembler = createMock(Disassembler.class);
        expect(disassembler.disassemble(0x53)).andThrow(new InvalidInstructionException()).anyTimes();
        replay(disassembler);

        MnemoColumn column = new MnemoColumn(disassembler);

        assertEquals("[invalid]", column.getValue(0x53));

        verify(disassembler);
    }

    @Test
    public void testDisassembleIncompleteInstruction() throws Exception {
        Disassembler disassembler = createMock(Disassembler.class);
        expect(disassembler.disassemble(0x53)).andThrow(new IndexOutOfBoundsException()).anyTimes();
        replay(disassembler);

        MnemoColumn column = new MnemoColumn(disassembler);

        assertEquals("[incomplete]", column.getValue(0x53));

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

        assertEquals("some mnemo", column.getValue(0x53));

        verify(instruction, disassembler);
    }

    @Test
    public void testDefaultWidthIsNotPredefined() {
        assertEquals(-1, new MnemoColumn(createMock(Disassembler.class)).getDefaultWidth());
    }

    @Test
    public void testSetDebugValueJustForIncreasingCoverage() {
        new MnemoColumn(createMock(Disassembler.class)).setValue(-1, null);
    }
}
