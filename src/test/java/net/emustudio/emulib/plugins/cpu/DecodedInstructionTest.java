/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.cpu;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class DecodedInstructionTest {
    private DecodedInstruction instruction;

    @Before
    public void setUp() {
        instruction = new DecodedInstruction();
    }

    @Test
    public void testHasKeyForStringAndConstant() {
        instruction.add(0, "A", 5);
        assertTrue(instruction.hasKey(0));
    }

    @Test
    public void testHasKeyForBits() {
        instruction.add(0, 0, 8);
        assertTrue(instruction.hasKey(0));
    }

    @Test
    public void testNullStringForInvalidKey() {
        assertNull(instruction.getString(0));
    }

    @Test
    public void testNullBitsForInvalidKey() {
        assertNull(instruction.getBits(0));
    }

    @Test
    public void testNegativeConstantForInvalidKey() {
        assertEquals(-1, instruction.get(0));
    }

    @Test
    public void testGetStringAndConstant() {
        instruction.add(0, "A", 5);
        assertEquals("A", instruction.getString(0));
        assertEquals(5, instruction.get(0));
    }

    @Test
    public void testGetBits() {
        instruction.add(0, 0x0605, 16);
        assertEquals(0x0605, instruction.getBits(0).number);
    }

    @Test
    public void testOverwriteAbilityForStringAndConstant() {
        instruction.add(0, "A", 5);
        instruction.add(0, "B", 6);
        assertEquals(6, instruction.get(0));
        assertEquals("B", instruction.getString(0));
    }

    @Test
    public void testMultipleAdditionsSetLastKey() {
        instruction.add(0, "A", 5);
        instruction.add(0, "A", 6);
        assertEquals(1, instruction.getKeys().size());
        assertEquals(6, instruction.get(0));
    }

    @Test
    public void testSetImage() {
        instruction.setImage(new byte[] {6});
        assertArrayEquals(new byte[] {6}, instruction.getImage());
        assertEquals(1, instruction.getLength());
    }

}
