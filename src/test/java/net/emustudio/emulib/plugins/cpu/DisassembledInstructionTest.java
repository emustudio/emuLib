/*
 * This file is part of emuLib.
 *
 * Copyright (C) 2006-2020  Peter Jakubčo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.emustudio.emulib.plugins.cpu;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DisassembledInstructionTest {
    private DisassembledInstruction instruction;
    private static final String DEFAULT_MNEMO = "mnemo";
    private static final String DEFAULT_OPCODE = "opcode";
    private static final int DEFAULT_ADDRESS = 100;

    @Before
    public void setUp(){
        instruction = new DisassembledInstruction(
                DEFAULT_ADDRESS,
                DEFAULT_MNEMO,
                DEFAULT_OPCODE);
    }

    @Test
    public void testGetDefaultValues() {
        assertEquals(DEFAULT_ADDRESS, instruction.getAddress());
        assertEquals(DEFAULT_MNEMO, instruction.getMnemo());
        assertEquals(DEFAULT_OPCODE, instruction.getOpCode());
    }

    @Test
    public void testOverwriteInstruction() {
        String otherMnemo = DEFAULT_MNEMO + "AAA";
        String otherOpcode = DEFAULT_OPCODE + "BBB";

        instruction.setInstruction(otherMnemo, otherOpcode);
        assertEquals(otherMnemo, instruction.getMnemo());
        assertEquals(otherOpcode, instruction.getOpCode());
    }

    @Test
    public void testOverwriteAddress() {
        int otherAddress = DEFAULT_ADDRESS + 222;

        instruction.setAddress(otherAddress);
        assertEquals(otherAddress, instruction.getAddress());
    }


}
