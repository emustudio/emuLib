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

public class OpcodeColumnTest {

    @Test(expected = NullPointerException.class)
    public void testNullDisassemblerThrows() {
        new OpcodeColumn(null);
    }

    @Test
    public void testClassTypeIsString() {
        OpcodeColumn column = new OpcodeColumn(createMock(Disassembler.class));

        assertEquals(String.class, column.getClassType());
    }

    @Test
    public void testOpcodeColumnTitleIsCorrect() {
        OpcodeColumn column = new OpcodeColumn(createMock(Disassembler.class));

        assertEquals("opcode", column.getTitle());
    }

    @Test
    public void testDisassembleInvalidInstruction() throws Exception {
        Disassembler disassembler = createMock(Disassembler.class);
        expect(disassembler.disassemble(0x53)).andThrow(new InvalidInstructionException()).anyTimes();
        replay(disassembler);

        OpcodeColumn column = new OpcodeColumn(disassembler);

        assertEquals("", column.getValue(0x53));

        verify(disassembler);
    }

    @Test
    public void testDisassembleIncompleteInstruction() throws Exception {
        Disassembler disassembler = createMock(Disassembler.class);
        expect(disassembler.disassemble(0x53)).andThrow(new IndexOutOfBoundsException()).anyTimes();
        replay(disassembler);

        OpcodeColumn column = new OpcodeColumn(disassembler);

        assertEquals("", column.getValue(0x53));

        verify(disassembler);
    }

    @Test
    public void testOpcodeIsReturned() throws Exception {
        DisassembledInstruction instruction = createMock(DisassembledInstruction.class);
        expect(instruction.getOpCode()).andReturn("some opcode").once();
        replay(instruction);

        Disassembler disassembler = createMock(Disassembler.class);
        expect(disassembler.disassemble(0x53)).andReturn(instruction).once();
        replay(disassembler);

        OpcodeColumn column = new OpcodeColumn(disassembler);

        assertEquals("some opcode", column.getValue(0x53));

        verify(instruction, disassembler);
    }

    @Test
    public void testDefaultWidthIsNotPredefined() {
        assertEquals(-1, new OpcodeColumn(createMock(Disassembler.class)).getDefaultWidth());
    }

    @Test
    public void testSetDebugValueJustForIncreasingCoverage() {
        new OpcodeColumn(createMock(Disassembler.class)).setValue(-1, null);
    }

}
