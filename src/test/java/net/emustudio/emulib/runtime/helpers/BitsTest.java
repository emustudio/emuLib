/*
 * This file is part of emuLib.
 *
 * Copyright (C) 2006-2023  Peter Jakubčo
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
package net.emustudio.emulib.runtime.helpers;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class BitsTest {

    @Test
    public void testAbsolutePositiveIsKept() {
        Bits bits = new Bits(0x1D, 8);
        assertEquals(0x1D, bits.absolute().bits);
    }

    @Test
    public void testAbsoluteNegativeSignRemoved() {
        Bits bits = new Bits((byte)0x81, 8);
        assertEquals(127, bits.absolute().bits);
    }

    @Test
    public void testBitReverseWorks() {
        Bits bits = new Bits(0b11000111, 6);
        assertEquals(0b11111000, bits.reverseBits().bits);
    }

    @Test
    public void testShiftLeftWorks() {
        Bits bits = new Bits(0b11000000, 8);
        assertEquals(0b10000000, bits.shiftLeft().bits);
    }

    @Test
    public void testShiftRightWorks() {
        Bits bits = new Bits(0xFFFF, 16);
        assertEquals(0x7FFF, bits.shiftRight().bits);
    }

    @Test
    public void testReverseBytesWorks() {
        Bits bits = new Bits(0x04030201, 32);
        assertEquals(0x01020304, bits.reverseBytes().bits);
    }

    @Test
    public void testReverseBytesOneByte() {
        Bits bits = new Bits(0xFF, 8);
        assertEquals(0xFF, bits.reverseBytes().bits);
    }
}
