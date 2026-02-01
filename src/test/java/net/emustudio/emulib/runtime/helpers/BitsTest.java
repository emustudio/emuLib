/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.helpers;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BitsTest {

    @Test
    public void testAbsolutePositiveIsKept() {
        Bits bits = new Bits(0x1D, 8);
        assertEquals(0x1D, bits.absolute().number);
    }

    @Test
    public void testAbsoluteNegativeSignRemoved() {
        Bits bits = new Bits((byte)0x81, 8);
        assertEquals(127, bits.absolute().number);
    }

    @Test
    public void testBitReverseWorks() {
        Bits bits = new Bits(0b11000111, 6);
        assertEquals(0b11111000, bits.reverseBits().number);
    }

    @Test
    public void testShiftLeftWorks() {
        Bits bits = new Bits(0b11000000, 8);
        assertEquals(0b10000000, bits.shiftLeft().number);
    }

    @Test
    public void testShiftRightWorks() {
        Bits bits = new Bits(0xFFFF, 16);
        assertEquals(0x7FFF, bits.shiftRight().number);
    }

    @Test
    public void testReverseBytesWorks() {
        Bits bits = new Bits(0x04030201, 32);
        assertEquals(0x01020304, bits.reverseBytes().number);
    }

    @Test
    public void testReverseBytesOneByte() {
        Bits bits = new Bits(0xFF, 8);
        assertEquals(0xFF, bits.reverseBytes().number);
    }
}
