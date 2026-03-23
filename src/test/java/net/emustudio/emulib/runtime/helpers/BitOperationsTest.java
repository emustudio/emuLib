/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.helpers;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public class BitOperationsTest {

    @Test
    public void testEmptyPipelineKeepsInput() {
        Bits input = new Bits(0x1D, 8);

        Bits result = BitOperations.builder().apply(input);

        assertSame(input, result);
    }

    @Test
    public void testAbsolutePositiveIsKept() {
        Bits bits = new Bits(0x1D, 8);

        Bits result = BitOperations.builder()
                .absolute()
                .apply(bits);

        assertEquals(0x1D, result.number);
        assertEquals(0x1D, bits.number);
    }

    @Test
    public void testAbsoluteNegativeSignRemoved() {
        Bits bits = new Bits((byte) 0x81, 8);

        Bits result = BitOperations.builder()
                .absolute()
                .apply(bits);

        assertEquals(127, result.number);
        assertEquals((byte) 0x81, bits.number);
    }

    @Test
    public void testBitReverseWorks() {
        Bits bits = new Bits(0b11000111, 6);

        Bits result = BitOperations.builder()
                .reverseBits()
                .apply(bits);

        assertEquals(0b11111000, result.number);
        assertEquals(0b11000111, bits.number);
    }

    @Test
    public void testOperationsAreAppliedInOrder() {
        BitOperations operations = BitOperations.builder()
                .reverseBits()
                .shiftRight();

        Bits input = new Bits(0b00000011, 8);
        Bits result = operations.apply(input);

        assertEquals(0b01100000, result.number);
        assertEquals(0b00000011, input.number);
    }

    @Test
    public void testBuiltInAndCustomOperationsCanBeCombined() {
        BitOperations operations = BitOperations.builder()
                .then(bits -> new Bits(bits.number + 1, bits.length))
                .shiftLeft();

        Bits result = operations.apply(new Bits(0b010, 3));

        assertEquals(0b110, result.number);
        assertEquals(3, result.length);
    }

    @Test
    public void testShiftLeftWorks() {
        Bits bits = new Bits(0b11000000, 8);

        Bits result = BitOperations.builder()
                .shiftLeft()
                .apply(bits);

        assertEquals(0b10000000, result.number);
        assertEquals(0b11000000, bits.number);
    }

    @Test
    public void testShiftRightWorks() {
        Bits bits = new Bits(0xFFFF, 16);

        Bits result = BitOperations.builder()
                .shiftRight()
                .apply(bits);

        assertEquals(0x7FFF, result.number);
        assertEquals(0xFFFF, bits.number);
    }

    @Test
    public void testReverseBytesWorks() {
        Bits bits = new Bits(0x04030201, 32);

        Bits result = BitOperations.builder()
                .reverseBytes()
                .apply(bits);

        assertEquals(0x01020304, result.number);
        assertEquals(0x04030201, bits.number);
    }

    @Test
    public void testReverseBytesOneByte() {
        Bits bits = new Bits(0xFF, 8);

        Bits result = BitOperations.builder()
                .reverseBytes()
                .apply(bits);

        assertEquals(0xFF, result.number);
        assertEquals(0xFF, bits.number);
    }

    @Test
    public void testResultIsSeparateFromInputWhenOperationsRun() {
        BitOperations operations = BitOperations.builder()
                .shiftLeft();

        Bits input = new Bits(0b11, 2);
        Bits result = operations.apply(input);

        assertNotSame(input, result);
        assertEquals(0b11, input.number);
        assertEquals(0b10, result.number);
    }

    @Test
    public void testNullInputIsRejected() {
        try {
            BitOperations.builder().apply(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            assertEquals("bits must not be null", e.getMessage());
        }
    }

    @Test
    public void testNullOperationIsRejected() {
        try {
            BitOperations.builder().then(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            assertEquals("operation must not be null", e.getMessage());
        }
    }

    @Test
    public void testNullResultFromOperationIsRejected() {
        try {
            BitOperations.builder()
                    .then(bits -> null)
                    .apply(new Bits(1, 1));
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            assertEquals("operation must not return null", e.getMessage());
        }
    }
}
