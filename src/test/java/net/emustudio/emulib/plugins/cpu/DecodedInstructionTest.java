/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.cpu;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class DecodedInstructionTest {
    private static final byte HAS_BITS = 4;

    @Test
    public void testConstructorCopiesInputArrays() {
        byte[] image = new byte[] {6};
        byte[] keyStates = new byte[] {HAS_BITS, 0};
        int[] constants = new int[] {5, 6};
        String[] strings = new String[] {"A", "B"};
        int[] bitValues = new int[] {0x0605, 0x0708};
        int[] bitLengths = new int[] {16, 8};

        DecodedInstruction instruction = new DecodedInstruction(
                image, 1, keyStates, constants, strings, bitValues, bitLengths
        );

        image[0] = 7;
        keyStates[0] = 0;
        constants[0] = 9;
        strings[0] = "C";
        bitValues[0] = 0;
        bitLengths[0] = 0;

        assertArrayEquals(new byte[] {6}, instruction.image);
        assertArrayEquals(new int[] {5, 6}, instruction.constants);
        assertArrayEquals(new String[] {"A", "B"}, instruction.strings);
        assertEquals(1, instruction.keyCount);
        assertEquals(0x0605, instruction.bits[0].number);
        assertEquals(16, instruction.bits[0].length);
        assertNull(instruction.bits[1]);
    }

    @Test
    public void testBitsAreCreatedOnlyForKeysWithBitState() {
        DecodedInstruction instruction = new DecodedInstruction(
                new byte[] {6},
                2,
                new byte[] {0, HAS_BITS, 3, HAS_BITS},
                new int[] {0, 0, 5, 0},
                new String[] {null, null, "A", null},
                new int[] {0, 0x0605, 0, 0x0A09},
                new int[] {0, 16, 0, 16}
        );

        assertNull(instruction.bits[0]);
        assertEquals(0x0605, instruction.bits[1].number);
        assertEquals(16, instruction.bits[1].length);
        assertNull(instruction.bits[2]);
        assertEquals(0x0A09, instruction.bits[3].number);
        assertEquals(16, instruction.bits[3].length);
    }

    @Test
    public void testEqualsAndHashCodeDependOnImageOnly() {
        DecodedInstruction first = new DecodedInstruction(
                new byte[] {6},
                1,
                new byte[] {HAS_BITS},
                new int[] {5},
                new String[] {"A"},
                new int[] {0x0605},
                new int[] {16}
        );
        DecodedInstruction second = new DecodedInstruction(
                new byte[] {6},
                0,
                new byte[] {0},
                new int[] {9},
                new String[] {"B"},
                new int[] {0},
                new int[] {0}
        );
        DecodedInstruction different = new DecodedInstruction(
                new byte[] {7},
                1,
                new byte[] {HAS_BITS},
                new int[] {5},
                new String[] {"A"},
                new int[] {0x0605},
                new int[] {16}
        );

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, different);
        assertNotEquals(first, null);
        assertNotEquals(first, "instruction");
    }

    @Test
    public void testToStringReturnsImageString() {
        DecodedInstruction instruction = new DecodedInstruction(
                new byte[] {6, 7},
                0,
                new byte[0],
                new int[0],
                new String[0],
                new int[0],
                new int[0]
        );

        assertEquals(Arrays.toString(new byte[] {6, 7}), instruction.toString());
    }
}
