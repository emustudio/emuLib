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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class NumberUtilsTest {

    @Test
    public void testReverseBitsSmallerThanByte() {
        assertEquals(3, NumberUtils.reverseBits(6, 3));
    }

    @Test
    public void testReverseBitsTwoBytes() {
        assertEquals(0xD96D, NumberUtils.reverseBits(0xB69B, 16));
    }

    @Test
    public void testReverseBitsOneByteOfTwoBytes() {
        assertEquals(0xB6D9, NumberUtils.reverseBits(0xB69B, 8));
    }

    @Test
    public void testReverseBitsFullInteger() {
        assertEquals(0xD96DD96D, NumberUtils.reverseBits(0xB69BB69B, 32));
    }

    @Test
    public void testReverseBitsLong() {
        assertEquals(3L, NumberUtils.reverseBits(6L, 3));
        assertEquals(0xD96DL, NumberUtils.reverseBits(0xB69BL, 16));
        assertEquals(0xD9L, NumberUtils.reverseBits(0xB69BL, 8));
        assertEquals(0xD96DD96DL, NumberUtils.reverseBits(0xB69BB69BL, 32));
    }

    @Test
    public void testReadBitsLittleEndian() {
        // 0x12EFCDAB == 0b10010111011111100110110101011
        byte[] word = new byte[]{(byte) 0xAB, (byte)0xCD, (byte)0xEF, 0x12};
        int bits = NumberUtils.readBits(word, 12, 12, Strategy.LITTLE_ENDIAN);

        // 0b.....111011111100............
        // 0b111011111100 == 0xEFC
        assertEquals(0xEFC, bits);
    }

    @Test
    public void testReadBitsBigEndian() {
        // 0xABCDEF12 == 0b10101011110011011110111100010010
        byte[] word = new byte[]{(byte) 0xAB, (byte)0xCD, (byte)0xEF, 0x12};
        int bits = NumberUtils.readBits(word, 12, 12, Strategy.BIG_ENDIAN);

        // 0b........110011011110............
        // 0b110011011110 == 0xCDE
        assertEquals(0xCDE, bits);
    }

    @Test
    public void testReadIntFourBytesLittleEndian() {
        Integer[] word = new Integer[]{0xAB, 0xCD, 0xEF, 0x12};
        assertEquals(0x12EFCDAB, NumberUtils.readInt(word, Strategy.LITTLE_ENDIAN));
    }

    @Test
    public void testReadIntFourBytesBigEndian() {
        Byte[] word = new Byte[]{0xB, 6, 9, 0xB};
        assertEquals(0x0B06090B, NumberUtils.readInt(word, Strategy.BIG_ENDIAN));
    }

    @Test
    public void testReadNativeIntFourBytesLittleEndian() {
        int[] word = new int[]{0xAB, 0xCD, 0xEF, 0x12};
        assertEquals(0x12EFCDAB, NumberUtils.readInt(word, Strategy.LITTLE_ENDIAN));
    }

    @Test
    public void testReadNativeIntFourBytesBigEndian() {
        int[] word = new int[]{0xB, 6, 9, 0xB};
        assertEquals(0x0B06090B, NumberUtils.readInt(word, Strategy.BIG_ENDIAN));
    }

    @Test
    public void testReadIntFourBytesReverseBitsLittleEndian() {
        Byte[] word2 = new Byte[]{0xB, 6, 9, 0xB};
        int result = NumberUtils.readInt(word2, Strategy.REVERSE_BITS);
        assertEquals(String.format("Was: 0x%X", result), 0xD09060D0, result);
    }

    @Test
    public void testReadIntFourBytesReverseBitsBigEndian() {
        Byte[] word2 = new Byte[]{0xB, 6, 9, 0xB};
        int result = NumberUtils.readInt(word2, Strategy.REVERSE_BITS | Strategy.BIG_ENDIAN);
        assertEquals(String.format("Was: 0x%X", result),0xD06090D0, result);
    }

    @Test
    public void testReadIntTwoBytesLittleEndian() {
        Integer[] word = new Integer[]{0xAB, 0xCD};
        assertEquals(0xCDAB, NumberUtils.readInt(word, Strategy.LITTLE_ENDIAN));
    }

    @Test
    public void testReadIntTwoBytesBigEndian() {
        Integer[] word = new Integer[]{0xAB, 0xCD};
        int result = NumberUtils.readInt(word, Strategy.BIG_ENDIAN);
        assertEquals(String.format("Was: 0x%X", result), 0xABCD, result);
    }

    @Test
    public void testReadIntDoesNotTakeMoreBytesIntoAccount() {
        Integer[] word = new Integer[]{0xAB, 0xCD, 0xEF, 0x12, 0x30, 0x50, 0xAA};
        assertEquals(0x12EFCDAB, NumberUtils.readInt(word, Strategy.LITTLE_ENDIAN));
    }

    @Test
    public void testWriteIntLittleEndian() {
        Integer[] word = new Integer[4];
        NumberUtils.writeInt(0x12EFCDAB, word, Strategy.LITTLE_ENDIAN);
        assertArrayEquals(new Integer[]{0xAB, 0xCD, 0xEF, 0x12}, word);
    }

    @Test
    public void testWriteIntBigEndian() {
        Byte[] word = new Byte[4];
        NumberUtils.writeInt(0x0B06090B, word, Strategy.BIG_ENDIAN);
        assertArrayEquals(new Byte[]{0xB, 6, 9, 0xB}, word);
    }

    @Test
    public void testWriteIntBigEndianShort() {
        Short[] word = new Short[4];
        NumberUtils.writeInt(0x0B06090B, word, Strategy.BIG_ENDIAN);
        assertArrayEquals(new Short[]{0xB, 6, 9, 0xB}, word);
    }

    @Test
    public void testWriteIntReverseBitsLittleEndian() {
        Byte[] word = new Byte[4];
        NumberUtils.writeInt(0xD06090D0, word, Strategy.REVERSE_BITS);
        assertArrayEquals(new Byte[]{0xB, 9, 6, 0xB}, word);
    }

    @Test
    public void testWriteIntReverseBitsBigEndian() {
        Byte[] word = new Byte[4];
        NumberUtils.writeInt(0xD06090D0, word, Strategy.REVERSE_BITS | Strategy.BIG_ENDIAN);
        assertArrayEquals(new Byte[]{0xB, 6, 9, 0xB}, word);
    }

    @Test
    public void testWriteIntReverseBitsBigEndianNativeBytes() {
        byte[] word4 = new byte[4];
        NumberUtils.writeInt(0xD06090D0, word4, Strategy.REVERSE_BITS | Strategy.BIG_ENDIAN);
        assertArrayEquals(new byte[]{0xB, 9, 6, 0xB}, word4);
    }

    @Test
    public void testWriteIntReverseBitsBigEndianNativeInts() {
        int[] word5 = new int[4];
        NumberUtils.writeInt(0xD06090D0, word5, Strategy.REVERSE_BITS | Strategy.BIG_ENDIAN);
        assertArrayEquals(new int[]{0xB, 9, 6, 0xB}, word5);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testWriteIntFewBytes() {
        Byte[] word = new Byte[2];
        NumberUtils.writeInt(2, word, Strategy.LITTLE_ENDIAN);
    }

    @Test
    public void testWriteIntDoesNotModifyOtherBytes() {
        Byte[] word = new Byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
        NumberUtils.writeInt(0x0B06090B, word, Strategy.BIG_ENDIAN);
        assertArrayEquals(new Byte[]{0xB, 6, 9, 0xB, 5, 6, 7, 8, 9}, word);
    }

    @Test
    public void testWriteThenReadReversedGivesBackTheSameResult() {
        Byte[] word = new Byte[]{0, 0, 0, 0};

        NumberUtils.writeInt(0x123456, word, Strategy.REVERSE_BITS);
        int result = NumberUtils.readInt(word, Strategy.REVERSE_BITS);

        assertEquals(0x123456, result);
    }

    @Test
    public void testNativeBytesToBytes() {
        byte[] vals = new byte[]{0, 1, 2, 3, 4, 5, 6};
        Byte[] objVals = new Byte[]{0, 1, 2, 3, 4, 5, 6};

        assertArrayEquals(objVals, NumberUtils.nativeBytesToBytes(vals));
    }

    @Test
    public void testNumbersToBytes() {
        Number[] numbers = new Number[]{1, 2, 3, 4, 5, 6};
        Byte[] expected = new Byte[]{1, 2, 3, 4, 5, 6};

        assertArrayEquals(expected, NumberUtils.numbersToBytes(numbers));
    }

    @Test
    public void testNumbersToNativeBytes() {
        Number[] numbers = new Number[]{1, 2, 3, 4, 5, 6};
        byte[] expected = new byte[]{1, 2, 3, 4, 5, 6};

        assertArrayEquals(expected, NumberUtils.numbersToNativeBytes(numbers));
    }

    @Test
    public void testShortsToBytes() {
        Short[] numbers = new Short[]{1, 2, 3, 4, 5, 6};
        Byte[] expected = new Byte[]{1, 2, 3, 4, 5, 6};

        assertArrayEquals(expected, NumberUtils.shortsToBytes(numbers));
    }

    @Test
    public void testShortsToNativeBytes() {
        Short[] numbers = new Short[]{1, 2, 3, 4, 5, 6};
        byte[] expected = new byte[]{1, 2, 3, 4, 5, 6};

        assertArrayEquals(expected, NumberUtils.shortsToNativeBytes(numbers));
    }

    @Test
    public void testShortsToNativeShorts() {
        Short[] numbers = new Short[]{1, 2, 3, 4, 5, 6};
        short[] expected = new short[]{1, 2, 3, 4, 5, 6};

        assertArrayEquals(expected, NumberUtils.shortsToNativeShorts(numbers));
    }

    @Test
    public void testNativeShortsToNativeBytes() {
        short[] numbers = new short[]{1, 2, 3, 4, 5, 6};
        byte[] expected = new byte[]{1, 2, 3, 4, 5, 6};

        assertArrayEquals(expected, NumberUtils.nativeShortsToNativeBytes(numbers));
    }

    @Test
    public void testNativeShortsToBytes() {
        short[] numbers = new short[]{1, 2, 3, 4, 5, 6};
        Byte[] expected = new Byte[]{1, 2, 3, 4, 5, 6};

        assertArrayEquals(expected, NumberUtils.nativeShortsToBytes(numbers));
    }

    @Test
    public void testNativeShortsToShorts() {
        short[] numbers = new short[]{1, 2, 3, 4, 5, 6};
        Short[] expected = new Short[]{1, 2, 3, 4, 5, 6};

        assertArrayEquals(expected, NumberUtils.nativeShortsToShorts(numbers));
    }
}
