/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
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
        assertEquals(DEFAULT_ADDRESS, instruction.address);
        assertEquals(DEFAULT_MNEMO, instruction.mnemo);
        assertEquals(DEFAULT_OPCODE, instruction.opCode);
    }
}
