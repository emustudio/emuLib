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
package net.emustudio.emulib.runtime.helpers;

import net.emustudio.emulib.runtime.helpers.NumberUtils.Strategy;
import org.junit.Test;

import java.util.List;

import static net.emustudio.emulib.runtime.helpers.NumberUtils.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class NumberUtilsTest {

    @Test
    public void testReverseBitsSmallerThanByte() {
        assertEquals(3, reverseBits(6, 3));
    }

    @Test
    public void testReverseBitsTwoBytes() {
        assertEquals(0xD96D, reverseBits(0xB69B, 16));
    }

    @Test
    public void testReverseBitsOneByteOfTwoBytes() {
        assertEquals(0xB6D9, reverseBits(0xB69B, 8));
    }

    @Test
    public void testReverseBitsFullInteger() {
        assertEquals(0xD96DD96D, reverseBits(0xB69BB69B, 32));
    }

    @Test
    public void testReverseBitsLong() {
        assertEquals(3L, reverseBits(6L, 3));
        assertEquals(0xD96DL, reverseBits(0xB69BL, 16));
        assertEquals(0xD9L, reverseBits(0xB69BL, 8));
        assertEquals(0xD96DD96DL, reverseBits(0xB69BB69BL, 32));
    }

    @Test
    public void testReadBitsLittleEndian() {
        // 0x12EFCDAB == 0b10010111011111100110110101011
        // 111011111100 == 0xEFC
        // 111111001101
        byte[] word = new byte[]{(byte) 0xAB, (byte) 0xCD, (byte) 0xEF, 0x12};
        int bits = readBits(word, 12, 12, Strategy.LITTLE_ENDIAN);

        assertEquals(0xEFC, bits);
    }

    @Test
    public void testReadBitsLittleEndianShifted() {
        //  0xAFFA == 0b1010111111111010
        //  01111111 = 0x7F
        byte[] word = new byte[]{(byte) 0xFA, (byte) 0xAF, 0, 0};
        int bits = readBits(word, 5, 8, Strategy.LITTLE_ENDIAN);
        assertEquals(0x7F, bits);
    }

    @Test
    public void testReadBitsBigEndian() {
        // 0xABCDEF12 == 0b10101011110011011110111100010010
        // 110111101111 = 0xDEF
        byte[] word = new byte[]{(byte) 0xAB, (byte) 0xCD, (byte) 0xEF, 0x12};
        int bits = readBits(word, 12, 12, Strategy.BIG_ENDIAN);
        assertEquals(0xDEF, bits);
    }

    @Test
    public void testReadBitsBigEndianShifted() {
        //  0xFAAF == 0b1111101010101111
        //  01010101
        byte[] word = new byte[]{(byte) 0xFA, (byte) 0xAF, 0, 0};
        int bits = readBits(word, 5, 8, Strategy.BIG_ENDIAN);
        assertEquals(0x55, bits);
    }

    @Test
    public void testReadBitsMaxInteger() {
        int number = readBits(new byte[]{
            0x20, (byte) 0x82, 0x25, 0x60
        }, 0, 32, Strategy.LITTLE_ENDIAN);
        assertEquals(0x60258220, number);
    }

    @Test
    public void testReadIntFourBytesLittleEndian() {
        Integer[] word = new Integer[]{0xAB, 0xCD, 0xEF, 0x12};
        assertEquals(0x12EFCDAB, readInt(word, Strategy.LITTLE_ENDIAN));
    }

    @Test
    public void testReadIntFourBytesBigEndian() {
        Byte[] word = new Byte[]{0xB, 6, 9, 0xB};
        assertEquals(0x0B06090B, readInt(word, Strategy.BIG_ENDIAN));
    }

    @Test
    public void testReadNativeIntFourBytesLittleEndian() {
        int[] word = new int[]{0xAB, 0xCD, 0xEF, 0x12};
        assertEquals(0x12EFCDAB, readInt(word, Strategy.LITTLE_ENDIAN));
    }

    @Test
    public void testReadNativeIntFourBytesBigEndian() {
        int[] word = new int[]{0xB, 6, 9, 0xB};
        assertEquals(0x0B06090B, readInt(word, Strategy.BIG_ENDIAN));
    }

    @Test
    public void testReadIntFourBytesReverseBitsLittleEndian() {
        Byte[] word2 = new Byte[]{0xB, 6, 9, 0xB};
        int result = readInt(word2, Strategy.REVERSE_BITS);
        assertEquals(String.format("Was: 0x%X", result), 0xD09060D0, result);
    }

    @Test
    public void testReadIntFourBytesReverseBitsBigEndian() {
        Byte[] word2 = new Byte[]{0xB, 6, 9, 0xB};
        int result = readInt(word2, Strategy.REVERSE_BITS | Strategy.BIG_ENDIAN);
        assertEquals(String.format("Was: 0x%X", result), 0xD06090D0, result);
    }

    @Test
    public void testReadIntTwoBytesLittleEndian() {
        Integer[] word = new Integer[]{0xAB, 0xCD};
        assertEquals(0xCDAB, readInt(word, Strategy.LITTLE_ENDIAN));
    }

    @Test
    public void testReadIntTwoBytesBigEndian() {
        Integer[] word = new Integer[]{0xAB, 0xCD};
        int result = readInt(word, Strategy.BIG_ENDIAN);
        assertEquals(String.format("Was: 0x%X", result), 0xABCD, result);
    }

    @Test
    public void testReadIntDoesNotTakeMoreBytesIntoAccount() {
        Integer[] word = new Integer[]{0xAB, 0xCD, 0xEF, 0x12, 0x30, 0x50, 0xAA};
        assertEquals(0x12EFCDAB, readInt(word, Strategy.LITTLE_ENDIAN));
    }

    @Test
    public void testWriteIntLittleEndian() {
        Integer[] word = new Integer[4];
        writeInt(0x12EFCDAB, word, Strategy.LITTLE_ENDIAN);
        assertArrayEquals(new Integer[]{0xAB, 0xCD, 0xEF, 0x12}, word);
    }

    @Test
    public void testWriteIntBigEndian() {
        Byte[] word = new Byte[4];
        writeInt(0x0B06090B, word, Strategy.BIG_ENDIAN);
        assertArrayEquals(new Byte[]{0xB, 6, 9, 0xB}, word);
    }

    @Test
    public void testWriteIntBigEndianShort() {
        Short[] word = new Short[4];
        writeInt(0x0B06090B, word, Strategy.BIG_ENDIAN);
        assertArrayEquals(new Short[]{0xB, 6, 9, 0xB}, word);
    }

    @Test
    public void testWriteIntReverseBitsLittleEndian() {
        Byte[] word = new Byte[4];
        writeInt(0xD06090D0, word, Strategy.REVERSE_BITS);
        assertArrayEquals(new Byte[]{0xB, 9, 6, 0xB}, word);
    }

    @Test
    public void testWriteIntReverseBitsBigEndian() {
        Byte[] word = new Byte[4];
        writeInt(0xD06090D0, word, Strategy.REVERSE_BITS | Strategy.BIG_ENDIAN);
        assertArrayEquals(new Byte[]{0xB, 6, 9, 0xB}, word);
    }

    @Test
    public void testWriteIntReverseBitsBigEndianNativeBytes() {
        byte[] word4 = new byte[4];
        writeInt(0xD06090D0, word4, Strategy.REVERSE_BITS | Strategy.BIG_ENDIAN);
        assertArrayEquals(new byte[]{0xB, 9, 6, 0xB}, word4);
    }

    @Test
    public void testWriteIntReverseBitsBigEndianNativeInts() {
        int[] word5 = new int[4];
        writeInt(0xD06090D0, word5, Strategy.REVERSE_BITS | Strategy.BIG_ENDIAN);
        assertArrayEquals(new int[]{0xB, 9, 6, 0xB}, word5);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testWriteIntFewBytes() {
        Byte[] word = new Byte[2];
        writeInt(2, word, Strategy.LITTLE_ENDIAN);
    }

    @Test
    public void testWriteIntDoesNotModifyOtherBytes() {
        Byte[] word = new Byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
        writeInt(0x0B06090B, word, Strategy.BIG_ENDIAN);
        assertArrayEquals(new Byte[]{0xB, 6, 9, 0xB, 5, 6, 7, 8, 9}, word);
    }

    @Test
    public void testWriteThenReadReversedGivesBackTheSameResult() {
        Byte[] word = new Byte[]{0, 0, 0, 0};

        writeInt(0x123456, word, Strategy.REVERSE_BITS);
        int result = readInt(word, Strategy.REVERSE_BITS);

        assertEquals(0x123456, result);
    }

    @Test
    public void testNativeBytesToBytes() {
        byte[] vals = new byte[]{0, 1, 2, 3, 4, 5, 6};
        Byte[] objVals = new Byte[]{0, 1, 2, 3, 4, 5, 6};
        assertArrayEquals(objVals, nativeBytesToBytes(vals));
    }

    @Test
    public void testNumbersToBytes() {
        Number[] numbers = new Number[]{1, 2, 3, 4, 5, 6};
        Byte[] expected = new Byte[]{1, 2, 3, 4, 5, 6};
        assertArrayEquals(expected, numbersToBytes(numbers));
    }

    @Test
    public void testNumbersToNativeBytes() {
        Number[] numbers = new Number[]{1, 2, 3, 4, 5, 6};
        byte[] expected = new byte[]{1, 2, 3, 4, 5, 6};
        assertArrayEquals(expected, numbersToNativeBytes(numbers));
    }

    @Test
    public void testShortsToBytes() {
        Short[] numbers = new Short[]{1, 2, 3, 4, 5, 6};
        Byte[] expected = new Byte[]{1, 2, 3, 4, 5, 6};
        assertArrayEquals(expected, shortsToBytes(numbers));
    }

    @Test
    public void testShortsToNativeBytes() {
        Short[] numbers = new Short[]{1, 2, 3, 4, 5, 6};
        byte[] expected = new byte[]{1, 2, 3, 4, 5, 6};
        assertArrayEquals(expected, shortsToNativeBytes(numbers));
    }

    @Test
    public void testShortsToNativeShorts() {
        Short[] numbers = new Short[]{1, 2, 3, 4, 5, 6};
        short[] expected = new short[]{1, 2, 3, 4, 5, 6};
        assertArrayEquals(expected, shortsToNativeShorts(numbers));
    }

    @Test
    public void testNativeShortsToNativeBytes() {
        short[] numbers = new short[]{1, 2, 3, 4, 5, 6};
        byte[] expected = new byte[]{1, 2, 3, 4, 5, 6};
        assertArrayEquals(expected, nativeShortsToNativeBytes(numbers));
    }

    @Test
    public void testNativeShortsToBytes() {
        short[] numbers = new short[]{1, 2, 3, 4, 5, 6};
        Byte[] expected = new Byte[]{1, 2, 3, 4, 5, 6};
        assertArrayEquals(expected, nativeShortsToBytes(numbers));
    }

    @Test
    public void testNativeShortsToShorts() {
        short[] numbers = new short[]{1, 2, 3, 4, 5, 6};
        Short[] expected = new Short[]{1, 2, 3, 4, 5, 6};
        assertArrayEquals(expected, nativeShortsToShorts(numbers));
    }

    @Test
    public void testListToNativeInts() {
        List<Integer> list = List.of(1, 2, 3, 4);
        int[] expected = new int[]{1, 2, 3, 4};
        assertArrayEquals(expected, listToNativeInts(list));
    }

    @Test
    public void testBcd2Bin() {
        assertEquals(44, bcd2bin(0x44));
        assertEquals(99, bcd2bin(0x99));
        assertEquals(0, bcd2bin(0x00));
        assertEquals(1, bcd2bin(0x01));
        assertEquals(10, bcd2bin(0x10));
    }

    @Test
    public void testBin2Bcd() {
        assertEquals(0x22, bin2bcd(22));
        assertEquals(0x99, bin2bcd(99));
        assertEquals(0x62, bin2bcd(62));
        assertEquals(0x81, bin2bcd(81));
    }
}
