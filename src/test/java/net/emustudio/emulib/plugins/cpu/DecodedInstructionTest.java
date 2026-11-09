/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.cpu;

import org.junit.Test;

import static org.junit.Assert.*;

public class DecodedInstructionTest {
    @Test
    public void capturesInstructionTextAndCellExtent() {
        DecodedInstruction instruction = DecodedInstruction.of(7, 1, "read =42", "READ CONSTANT 42");

        assertEquals(7, instruction.address());
        assertEquals(1, instruction.length());
        assertEquals("read =42", instruction.mnemonic());
        assertEquals("READ CONSTANT 42", instruction.encoding());
    }

    @Test
    public void keepsExtentIndependentOfRuntimeProgramCounter() {
        DecodedInstruction instruction = DecodedInstruction.of(65535, 2, "instruction", "AA BB");

        assertEquals(65535, instruction.address());
        assertEquals(2, instruction.length());
    }

    @Test
    public void rejectsInvalidInstructionFacts() {
        assertThrows(IllegalArgumentException.class, () -> DecodedInstruction.of(-1, 1, "", ""));
        assertThrows(IllegalArgumentException.class, () -> DecodedInstruction.of(0, 0, "", ""));
        assertThrows(NullPointerException.class, () -> DecodedInstruction.of(0, 1, null, ""));
        assertThrows(NullPointerException.class, () -> DecodedInstruction.of(0, 1, "", null));
    }
}
