/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.helpers;

import net.emustudio.emulib.runtime.helpers.NumberUtils.Strategy;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static net.emustudio.emulib.runtime.helpers.NumberUtils.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class NumberUtilsTest {

    @Test
    public void testReverseBitsInt() {
        assertEquals(3, reverseBits(6, 3));
        assertEquals(0xD96D, reverseBits(0xB69B, 16));
        assertEquals(0xB6D9, reverseBits(0xB69B, 8));
        assertEquals(0xD96DD96D, reverseBits(0xB69BB69B, 32));
        // zero/negative bits = identity
        assertEquals(0x12345678, reverseBits(0x12345678, 0));
        assertEquals(0x12345678, reverseBits(0x12345678, -1));
        assertEquals(0x12345678, reverseBits(0x12345678, -100));
        // more than 32 bits = full reverse
        assertEquals(Integer.reverse(0x12345678), reverseBits(0x12345678, 33));
        assertEquals(Integer.reverse(0x12345678), reverseBits(0x12345678, 64));
        // single bit
        assertEquals(0, reverseBits(0, 1));
        assertEquals(1, reverseBits(1, 1));
    }

    @Test
    public void testReverseBitsLong() {
        assertEquals(3L, reverseBits(6L, 3));
        assertEquals(0xD96DL, reverseBits(0xB69BL, 16));
        assertEquals(0xD9L, reverseBits(0xB69BL, 8));
        assertEquals(0xD96DD96DL, reverseBits(0xB69BB69BL, 32));
        // zero/negative bits
        assertEquals(0L, reverseBits(0x12345678L, 0));
        assertEquals(0L, reverseBits(0x12345678L, -1));
        assertEquals(0L, reverseBits(0x12345678L, -100));
        // 64 bits = full reverse
        assertEquals(Long.reverse(0x123456789ABCDEF0L), reverseBits(0x123456789ABCDEF0L, 64));
        // more than 64 bits = full reverse
        assertEquals(Long.reverse(0x123456789ABCDEF0L), reverseBits(0x123456789ABCDEF0L, 65));
        assertEquals(Long.reverse(0x123456789ABCDEF0L), reverseBits(0x123456789ABCDEF0L, 100));
        // single bit
        assertEquals(0L, reverseBits(0L, 1));
        assertEquals(1L, reverseBits(1L, 1));
    }

    @Test
    public void testReadBitsLittleEndian() {
        byte[] word = new byte[]{(byte) 0xAB, (byte) 0xCD, (byte) 0xEF, 0x12};
        assertEquals(0xEFC, readBits(word, 12, 12, Strategy.LITTLE_ENDIAN));

        byte[] word2 = new byte[]{(byte) 0xFA, (byte) 0xAF, 0, 0};
        assertEquals(0x7F, readBits(word2, 5, 8, Strategy.LITTLE_ENDIAN));

        assertEquals(0x60258220, readBits(new byte[]{0x20, (byte) 0x82, 0x25, 0x60}, 0, 32, Strategy.LITTLE_ENDIAN));
        assertEquals(0xAB, readBits(new byte[]{(byte) 0xAB}, 0, 8, Strategy.LITTLE_ENDIAN));
        assertEquals(1, readBits(new byte[]{0x01}, 0, 1, Strategy.LITTLE_ENDIAN));
        assertEquals(0, readBits(new byte[]{0x01}, 1, 1, Strategy.LITTLE_ENDIAN));
        // start beyond first byte
        assertEquals(0xAB, readBits(new byte[]{(byte) 0xFF, (byte) 0xAB}, 8, 8, Strategy.LITTLE_ENDIAN));
    }

    @Test
    public void testReadBitsBigEndian() {
        byte[] word = new byte[]{(byte) 0xAB, (byte) 0xCD, (byte) 0xEF, 0x12};
        assertEquals(0xDEF, readBits(word, 12, 12, Strategy.BIG_ENDIAN));

        byte[] word2 = new byte[]{(byte) 0xFA, (byte) 0xAF, 0, 0};
        assertEquals(0x55, readBits(word2, 5, 8, Strategy.BIG_ENDIAN));

        assertEquals(0x60258220, readBits(new byte[]{0x60, 0x25, (byte) 0x82, 0x20}, 0, 32, Strategy.BIG_ENDIAN));
        assertEquals(0xAB, readBits(new byte[]{(byte) 0xAB}, 0, 8, Strategy.BIG_ENDIAN));
        assertEquals(1, readBits(new byte[]{(byte) 0x80}, 0, 1, Strategy.BIG_ENDIAN));
        assertEquals(0, readBits(new byte[]{(byte) 0x80}, 1, 1, Strategy.BIG_ENDIAN));
    }

    @Test
    public void testReadBitsReverseBits() {
        // 0xAB = 10101011, reversed = 11010101 = 0xD5
        byte[] word = new byte[]{(byte) 0xAB};
        assertEquals(0xD5, readBits(word, 0, 8, Strategy.LITTLE_ENDIAN | Strategy.REVERSE_BITS));
        assertEquals(0xD5, readBits(word, 0, 8, Strategy.BIG_ENDIAN | Strategy.REVERSE_BITS));
    }

    @Test
    public void testReadIntLittleEndian() {
        // Integer[]
        assertEquals(0x12EFCDAB, readInt(new Integer[]{0xAB, 0xCD, 0xEF, 0x12}, Strategy.LITTLE_ENDIAN));
        assertEquals(0xCDAB, readInt(new Integer[]{0xAB, 0xCD}, Strategy.LITTLE_ENDIAN));
        assertEquals(0x12EFCDAB, readInt(new Integer[]{0xAB, 0xCD, 0xEF, 0x12, 0x30, 0x50, 0xAA}, Strategy.LITTLE_ENDIAN));
        // int[]
        assertEquals(0x12EFCDAB, readInt(new int[]{0xAB, 0xCD, 0xEF, 0x12}, Strategy.LITTLE_ENDIAN));
        assertEquals(0xCDAB, readInt(new int[]{0xAB, 0xCD}, Strategy.LITTLE_ENDIAN));
        // byte[]
        assertEquals(0x12EFCDAB, readInt(new byte[]{(byte) 0xAB, (byte) 0xCD, (byte) 0xEF, 0x12}, Strategy.LITTLE_ENDIAN));
        // Byte[]
        assertEquals(0x12EFCDAB, readInt(new Byte[]{(byte) 0xAB, (byte) 0xCD, (byte) 0xEF, 0x12}, Strategy.LITTLE_ENDIAN));
        assertEquals(0xCDAB, readInt(new Byte[]{(byte) 0xAB, (byte) 0xCD}, Strategy.LITTLE_ENDIAN));
        assertEquals(0xAB, readInt(new Byte[]{(byte) 0xAB}, Strategy.LITTLE_ENDIAN));
    }

    @Test
    public void testReadIntBigEndian() {
        // Byte[]
        assertEquals(0x0B06090B, readInt(new Byte[]{0xB, 6, 9, 0xB}, Strategy.BIG_ENDIAN));
        assertEquals(0xABCD, readInt(new Byte[]{(byte) 0xAB, (byte) 0xCD}, Strategy.BIG_ENDIAN));
        assertEquals(0xAB, readInt(new Byte[]{(byte) 0xAB}, Strategy.BIG_ENDIAN));
        // Integer[]
        assertEquals(0xABCD, readInt(new Integer[]{0xAB, 0xCD}, Strategy.BIG_ENDIAN));
        // int[]
        assertEquals(0x0B06090B, readInt(new int[]{0xB, 6, 9, 0xB}, Strategy.BIG_ENDIAN));
        assertEquals(0xABCD, readInt(new int[]{0xAB, 0xCD}, Strategy.BIG_ENDIAN));
        // byte[]
        assertEquals(0xABCDEF12, readInt(new byte[]{(byte) 0xAB, (byte) 0xCD, (byte) 0xEF, 0x12}, Strategy.BIG_ENDIAN));
    }

    @Test
    public void testReadIntReverseBits() {
        // Byte[]
        assertEquals(0xD09060D0, readInt(new Byte[]{0xB, 6, 9, 0xB}, Strategy.REVERSE_BITS));
        assertEquals(0xD06090D0, readInt(new Byte[]{0xB, 6, 9, 0xB}, Strategy.REVERSE_BITS | Strategy.BIG_ENDIAN));
        // Integer[]
        assertEquals(0xD09060D0, readInt(new Integer[]{0x0B, 0x06, 0x09, 0x0B}, Strategy.REVERSE_BITS));
        assertEquals(0xD06090D0, readInt(new Integer[]{0x0B, 0x06, 0x09, 0x0B}, Strategy.REVERSE_BITS | Strategy.BIG_ENDIAN));
        // int[]
        assertEquals(0xD09060D0, readInt(new int[]{0x0B, 0x06, 0x09, 0x0B}, Strategy.REVERSE_BITS));
        assertEquals(0xD06090D0, readInt(new int[]{0x0B, 0x06, 0x09, 0x0B}, Strategy.REVERSE_BITS | Strategy.BIG_ENDIAN));
        // byte[]
        assertEquals(0xD09060D0, readInt(new byte[]{0xB, 6, 9, 0xB}, Strategy.REVERSE_BITS));
        assertEquals(0xD06090D0, readInt(new byte[]{0xB, 6, 9, 0xB}, Strategy.REVERSE_BITS | Strategy.BIG_ENDIAN));
    }

    @Test
    public void testReadIntWithOffset() {
        byte[] word = new byte[]{0x55, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF, 0x12, 0x22};
        assertEquals(0x12EFCDAB, readInt(word, 1, 4, Strategy.LITTLE_ENDIAN));
        assertEquals(0xABCDEF12, readInt(word, 1, 4, Strategy.BIG_ENDIAN));

        byte[] word2 = new byte[]{0x55, (byte) 0xAB, (byte) 0xCD, 0x22};
        assertEquals(0xCDAB, readInt(word2, 1, 2, Strategy.LITTLE_ENDIAN));
        assertEquals(0xABCD, readInt(word2, 1, 2, Strategy.BIG_ENDIAN));

        byte[] word3 = new byte[]{0x55, (byte) 0xAB, 0x22};
        assertEquals(0xAB, readInt(word3, 1, 1, Strategy.LITTLE_ENDIAN));
        assertEquals(0xAB, readInt(word3, 1, 1, Strategy.BIG_ENDIAN));

        assertEquals(0, readInt(new byte[]{0x55}, 0, 0, Strategy.LITTLE_ENDIAN));
        assertEquals(0xD06090D0, readInt(new byte[]{0x55, 0x0B, 6, 9, 0x0B, 0x22}, 1, 4, Strategy.REVERSE_BITS | Strategy.BIG_ENDIAN));
    }

    @Test
    public void testReadIntWithNegativeBytes() {
        byte[] word = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        assertEquals(0xFFFFFFFF, readInt(word, Strategy.LITTLE_ENDIAN));
        assertEquals(0xFFFFFFFF, readInt(word, Strategy.BIG_ENDIAN));
    }

    @Test
    public void testWriteIntLittleEndian() {
        Integer[] iWord = new Integer[4];
        writeInt(0x12EFCDAB, iWord, Strategy.LITTLE_ENDIAN);
        assertArrayEquals(new Integer[]{0xAB, 0xCD, 0xEF, 0x12}, iWord);

        int[] niWord = new int[4];
        writeInt(0x12EFCDAB, niWord, Strategy.LITTLE_ENDIAN);
        assertArrayEquals(new int[]{0xAB, 0xCD, 0xEF, 0x12}, niWord);

        Byte[] bWord = new Byte[4];
        writeInt(0x12EFCDAB, bWord, Strategy.LITTLE_ENDIAN);
        assertArrayEquals(new Byte[]{(byte) 0xAB, (byte) 0xCD, (byte) 0xEF, 0x12}, bWord);

        byte[] nbWord = new byte[4];
        writeInt(0x12EFCDAB, nbWord, Strategy.LITTLE_ENDIAN);
        assertArrayEquals(new byte[]{(byte) 0xAB, (byte) 0xCD, (byte) 0xEF, 0x12}, nbWord);

        Short[] sWord = new Short[4];
        writeInt(0x12EFCDAB, sWord, Strategy.LITTLE_ENDIAN);
        assertArrayEquals(new Short[]{(short) 0xAB, (short) 0xCD, (short) 0xEF, (short) 0x12}, sWord);
    }

    @Test
    public void testWriteIntBigEndian() {
        Byte[] bWord = new Byte[4];
        writeInt(0x0B06090B, bWord, Strategy.BIG_ENDIAN);
        assertArrayEquals(new Byte[]{0xB, 6, 9, 0xB}, bWord);

        Short[] sWord = new Short[4];
        writeInt(0x0B06090B, sWord, Strategy.BIG_ENDIAN);
        assertArrayEquals(new Short[]{0xB, 6, 9, 0xB}, sWord);

        Integer[] iWord = new Integer[4];
        writeInt(0x12EFCDAB, iWord, Strategy.BIG_ENDIAN);
        assertArrayEquals(new Integer[]{0x12, 0xEF, 0xCD, 0xAB}, iWord);

        int[] niWord = new int[4];
        writeInt(0x12EFCDAB, niWord, Strategy.BIG_ENDIAN);
        assertArrayEquals(new int[]{0x12, 0xEF, 0xCD, 0xAB}, niWord);

        byte[] nbWord = new byte[4];
        writeInt(0x12EFCDAB, nbWord, Strategy.BIG_ENDIAN);
        assertArrayEquals(new byte[]{0x12, (byte) 0xEF, (byte) 0xCD, (byte) 0xAB}, nbWord);
    }

    @Test
    public void testWriteIntReverseBits() {
        // Byte[] uses per-byte reversal
        Byte[] bWord = new Byte[4];
        writeInt(0xD06090D0, bWord, Strategy.REVERSE_BITS);
        assertArrayEquals(new Byte[]{0xB, 9, 6, 0xB}, bWord);
        writeInt(0xD06090D0, bWord, Strategy.REVERSE_BITS | Strategy.BIG_ENDIAN);
        assertArrayEquals(new Byte[]{0xB, 6, 9, 0xB}, bWord);

        // byte[] uses Integer.reverse()
        byte[] nbWord = new byte[4];
        writeInt(0xD06090D0, nbWord, Strategy.REVERSE_BITS);
        assertArrayEquals(new byte[]{0xB, 6, 9, 0xB}, nbWord);
        writeInt(0xD06090D0, nbWord, Strategy.REVERSE_BITS | Strategy.BIG_ENDIAN);
        assertArrayEquals(new byte[]{0xB, 9, 6, 0xB}, nbWord);

        // Integer[] uses Integer.reverse()
        Integer[] iWord = new Integer[4];
        writeInt(0xD06090D0, iWord, Strategy.REVERSE_BITS);
        assertArrayEquals(new Integer[]{0x0B, 0x06, 0x09, 0x0B}, iWord);
        writeInt(0xD06090D0, iWord, Strategy.REVERSE_BITS | Strategy.BIG_ENDIAN);
        assertArrayEquals(new Integer[]{0x0B, 0x09, 0x06, 0x0B}, iWord);

        // int[] uses Integer.reverse()
        int[] niWord = new int[4];
        writeInt(0xD06090D0, niWord, Strategy.REVERSE_BITS);
        assertArrayEquals(new int[]{0x0B, 0x06, 0x09, 0x0B}, niWord);
        writeInt(0xD06090D0, niWord, Strategy.REVERSE_BITS | Strategy.BIG_ENDIAN);
        assertArrayEquals(new int[]{0xB, 9, 6, 0xB}, niWord);

        // Short[] uses Integer.reverse()
        Short[] sWord = new Short[4];
        writeInt(0xD06090D0, sWord, Strategy.REVERSE_BITS);
        assertArrayEquals(new Short[]{(short) 0x0B, (short) 0x06, (short) 0x09, (short) 0x0B}, sWord);
        writeInt(0xD06090D0, sWord, Strategy.REVERSE_BITS | Strategy.BIG_ENDIAN);
        assertArrayEquals(new Short[]{(short) 0x0B, (short) 0x09, (short) 0x06, (short) 0x0B}, sWord);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testWriteIntFewBytes() {
        writeInt(2, new Byte[2], Strategy.LITTLE_ENDIAN);
    }

    @Test
    public void testWriteIntEdgeCases() {
        // does not modify other bytes
        Byte[] word = new Byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
        writeInt(0x0B06090B, word, Strategy.BIG_ENDIAN);
        assertArrayEquals(new Byte[]{0xB, 6, 9, 0xB, 5, 6, 7, 8, 9}, word);

        // zero value
        Integer[] zWord = new Integer[4];
        writeInt(0, zWord, Strategy.LITTLE_ENDIAN);
        assertArrayEquals(new Integer[]{0, 0, 0, 0}, zWord);
        writeInt(0, zWord, Strategy.BIG_ENDIAN);
        assertArrayEquals(new Integer[]{0, 0, 0, 0}, zWord);

        // max value
        byte[] mWord = new byte[4];
        writeInt(0xFFFFFFFF, mWord, Strategy.LITTLE_ENDIAN);
        assertArrayEquals(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF}, mWord);
    }

    @Test
    public void testWriteThenReadRoundTrip() {
        // Byte[] reverse bits
        Byte[] bWord = new Byte[]{0, 0, 0, 0};
        writeInt(0x123456, bWord, Strategy.REVERSE_BITS);
        assertEquals(0x123456, readInt(bWord, Strategy.REVERSE_BITS));

        // Integer[] LE and BE
        Integer[] iWord = new Integer[4];
        writeInt(0xDEADBEEF, iWord, Strategy.LITTLE_ENDIAN);
        assertEquals(0xDEADBEEF, readInt(iWord, Strategy.LITTLE_ENDIAN));
        writeInt(0xDEADBEEF, iWord, Strategy.BIG_ENDIAN);
        assertEquals(0xDEADBEEF, readInt(iWord, Strategy.BIG_ENDIAN));

        // int[] LE
        int[] niWord = new int[4];
        writeInt(0xDEADBEEF, niWord, Strategy.LITTLE_ENDIAN);
        assertEquals(0xDEADBEEF, readInt(niWord, Strategy.LITTLE_ENDIAN));

        // byte[] BE
        byte[] nbWord = new byte[4];
        writeInt(0xDEADBEEF, nbWord, Strategy.BIG_ENDIAN);
        assertEquals(0xDEADBEEF, readInt(nbWord, Strategy.BIG_ENDIAN));

        // byte[] reverse bits - NOT symmetric (Integer.reverse vs per-byte reversal)
        byte[] rbWord = new byte[4];
        writeInt(0x123456, rbWord, Strategy.REVERSE_BITS | Strategy.LITTLE_ENDIAN);
        assertArrayEquals(new byte[]{0x00, 0x48, 0x2C, 0x6A}, rbWord);
    }

    @Test
    public void testArrayConversions() {
        // nativeBytesToBytes
        assertArrayEquals(new Byte[]{0, 1, 2, 3, 4, 5, 6}, nativeBytesToBytes(new byte[]{0, 1, 2, 3, 4, 5, 6}));
        assertArrayEquals(new Byte[0], nativeBytesToBytes(new byte[0]));

        // numbersToBytes / numbersToNativeBytes
        assertArrayEquals(new Byte[]{1, 2, 3, 4, 5, 6}, numbersToBytes(new Number[]{1, 2, 3, 4, 5, 6}));
        assertArrayEquals(new byte[]{1, 2, 3, 4, 5, 6}, numbersToNativeBytes(new Number[]{1, 2, 3, 4, 5, 6}));

        // Short[] conversions
        Short[] shorts = new Short[]{1, 2, 3, 4, 5, 6};
        assertArrayEquals(new Byte[]{1, 2, 3, 4, 5, 6}, shortsToBytes(shorts));
        assertArrayEquals(new byte[]{1, 2, 3, 4, 5, 6}, shortsToNativeBytes(shorts));
        assertArrayEquals(new short[]{1, 2, 3, 4, 5, 6}, shortsToNativeShorts(shorts));

        // short[] conversions
        short[] nShorts = new short[]{1, 2, 3, 4, 5, 6};
        assertArrayEquals(new byte[]{1, 2, 3, 4, 5, 6}, nativeShortsToNativeBytes(nShorts));
        assertArrayEquals(new Byte[]{1, 2, 3, 4, 5, 6}, nativeShortsToBytes(nShorts));
        assertArrayEquals(new Short[]{1, 2, 3, 4, 5, 6}, nativeShortsToShorts(nShorts));

        // nativeIntsToNativeBytes
        assertArrayEquals(new byte[]{1, 2, 3, (byte) 0xFF, 0}, nativeIntsToNativeBytes(new int[]{1, 2, 3, 0xFF, 0x100}));
        assertArrayEquals(new byte[0], nativeIntsToNativeBytes(new int[0]));

        // nativeBytes to Integer/Short/int arrays
        byte[] ba = new byte[]{0, 1, (byte) 0x80, (byte) 0xFF};
        assertArrayEquals(new Integer[]{0, 1, 0x80, 0xFF}, nativeBytesToIntegers(ba));
        assertArrayEquals(new Short[]{0, 1, (short) 0x80, (short) 0xFF}, nativeBytesToShorts(ba));
        assertArrayEquals(new int[]{0, 1, 0x80, 0xFF}, nativeBytesToInts(ba));
        assertArrayEquals(new Integer[0], nativeBytesToIntegers(new byte[0]));
    }

    @Test
    public void testListToNativeInts() {
        assertArrayEquals(new int[]{1, 2, 3, 4}, listToNativeInts(List.of(1, 2, 3, 4)));
        assertArrayEquals(new int[]{1, 2, 3, 4}, listToNativeInts(new LinkedList<>(List.of(1, 2, 3, 4))));
        assertArrayEquals(new int[]{10, 20, 30}, listToNativeInts(new ArrayList<>(List.of(10, 20, 30))));
        assertArrayEquals(new int[0], listToNativeInts(List.of()));
    }

    @Test
    public void testBcdConversions() {
        // bcd2bin
        assertEquals(44, bcd2bin(0x44));
        assertEquals(99, bcd2bin(0x99));
        assertEquals(0, bcd2bin(0x00));
        assertEquals(1, bcd2bin(0x01));
        assertEquals(10, bcd2bin(0x10));
        assertEquals(9, bcd2bin(0x09));
        assertEquals(19, bcd2bin(0x19));
        assertEquals(90, bcd2bin(0x90));

        // bin2bcd
        assertEquals(0x22, bin2bcd(22));
        assertEquals(0x99, bin2bcd(99));
        assertEquals(0x62, bin2bcd(62));
        assertEquals(0x81, bin2bcd(81));
        assertEquals(0x00, bin2bcd(0));
        assertEquals(0x09, bin2bcd(9));
        assertEquals(0x10, bin2bcd(10));
        assertEquals(0x50, bin2bcd(50));

        // round trip
        for (int i = 0; i < 100; i++) {
            assertEquals(i, bcd2bin(bin2bcd(i)));
        }
    }
}
