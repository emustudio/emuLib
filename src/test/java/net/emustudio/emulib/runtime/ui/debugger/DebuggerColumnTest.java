/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.ui.debugger;

import net.emustudio.emulib.plugins.cpu.DecodedInstruction;
import net.emustudio.emulib.plugins.cpu.Decoder;
import net.emustudio.emulib.plugins.cpu.InvalidInstructionException;
import org.junit.Test;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class DebuggerColumnTest {

    @Test
    public void testTextColumnDefaults() {
        DebuggerColumn<String> column = DebuggerColumn.text("t", Integer::toString);
        assertEquals(String.class, column.getClassType());
        assertEquals("t", column.getTitle());
        assertEquals("5", column.getValue(5));
        assertFalse(column.isEditable());
        assertEquals(-1, column.getDefaultWidth());
    }

    @Test
    public void testAddressColumn() {
        assertEquals("address", DebuggerColumn.address("%04X").getTitle());
        assertEquals("00FF", DebuggerColumn.address("%04X").getValue(0xFF));
        assertEquals("53", DebuggerColumn.address("%d").getValue(53));
    }

    @Test(expected = NullPointerException.class)
    public void testNullInstructionLookupThrows() {
        DebuggerColumn.mnemo(null);
    }

    @Test
    public void testCapturedInstructionColumns() throws Exception {
        Decoder instructions = createMock(Decoder.class);
        expect(instructions.decode(1)).andReturn(DecodedInstruction.of(1, 1, "mnemo", "code")).times(2);
        expect(instructions.decode(2)).andThrow(new InvalidInstructionException()).times(2);
        expect(instructions.decode(3)).andThrow(new IndexOutOfBoundsException()).times(2);
        replay(instructions);

        DebuggerColumn<String> mnemo = DebuggerColumn.mnemo(instructions);
        DebuggerColumn<String> opcode = DebuggerColumn.opcode(instructions);
        assertEquals("instruction", mnemo.getTitle());
        assertEquals("opcode", opcode.getTitle());
        assertEquals("mnemo", mnemo.getValue(1));
        assertEquals("code", opcode.getValue(1));
        assertEquals("[invalid]", mnemo.getValue(2));
        assertEquals("", opcode.getValue(2));
        assertEquals("[incomplete]", mnemo.getValue(3));
        assertEquals("", opcode.getValue(3));
        verify(instructions);
    }
}
