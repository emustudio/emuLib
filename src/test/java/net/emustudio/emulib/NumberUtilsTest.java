/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib;

import net.emustudio.emulib.NumberUtils.Strategy;
import org.junit.Test;

import static net.emustudio.emulib.NumberUtils.*;
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
    public void testReadInt() {
        Byte[] word = {(byte) 0xAB, (byte) 0xCD, (byte) 0xEF, 0x12};
        assertEquals(0x12EFCDAB, readInt(word, Strategy.LITTLE_ENDIAN));
        assertEquals(0xABCDEF12, readInt(word, Strategy.BIG_ENDIAN));
        assertEquals(0xCDAB, readInt(new Byte[]{(byte) 0xAB, (byte) 0xCD}, Strategy.LITTLE_ENDIAN));
        assertEquals(0xABCD, readInt(new Byte[]{(byte) 0xAB, (byte) 0xCD}, Strategy.BIG_ENDIAN));
        assertEquals(0xAB, readInt(new Byte[]{(byte) 0xAB}, Strategy.BIG_ENDIAN));
        assertEquals(0, readInt(new Byte[0], Strategy.BIG_ENDIAN));
        // extra cells are ignored, higher-order bits of cells are cut
        assertEquals(0x12EFCDAB, readInt(new Integer[]{0xAB, 0xCD, 0xEF, 0x12, 0x30, 0x50}, Strategy.LITTLE_ENDIAN));
        assertEquals(0x0201, readInt(new Short[]{0x101, 0x302}, Strategy.LITTLE_ENDIAN));
        assertEquals(0xFFFFFFFF, readInt(new Byte[]{-1, -1, -1, -1}, Strategy.BIG_ENDIAN));
    }

    @Test
    public void testReadIntReverseBits() {
        assertEquals(0xD09060D0, readInt(new Byte[]{0xB, 6, 9, 0xB}, Strategy.REVERSE_BITS));
        assertEquals(0xD06090D0, readInt(new Byte[]{0xB, 6, 9, 0xB}, Strategy.REVERSE_BITS | Strategy.BIG_ENDIAN));
        assertEquals(0xD09060D0, readInt(new Integer[]{0x0B, 0x06, 0x09, 0x0B}, Strategy.REVERSE_BITS));
    }

    @Test
    public void testWriteInt() {
        Byte[] word = new Byte[4];
        writeInt(0x12EFCDAB, word, Strategy.LITTLE_ENDIAN);
        assertArrayEquals(new Byte[]{(byte) 0xAB, (byte) 0xCD, (byte) 0xEF, 0x12}, word);
        writeInt(0x0B06090B, word, Strategy.BIG_ENDIAN);
        assertArrayEquals(new Byte[]{0xB, 6, 9, 0xB}, word);
        writeInt(0xD06090D0, word, Strategy.REVERSE_BITS);
        assertArrayEquals(new Byte[]{0xB, 9, 6, 0xB}, word);
        writeInt(0xD06090D0, word, Strategy.REVERSE_BITS | Strategy.BIG_ENDIAN);
        assertArrayEquals(new Byte[]{0xB, 6, 9, 0xB}, word);

        // does not modify other cells
        Byte[] longer = new Byte[]{1, 2, 3, 4, 5, 6};
        writeInt(0x0B06090B, longer, Strategy.BIG_ENDIAN);
        assertArrayEquals(new Byte[]{0xB, 6, 9, 0xB, 5, 6}, longer);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void testWriteIntFewBytes() {
        writeInt(2, new Byte[2], Strategy.LITTLE_ENDIAN);
    }

    @Test
    public void testWriteThenReadRoundTrip() {
        int[] strategies = {Strategy.LITTLE_ENDIAN, Strategy.BIG_ENDIAN, Strategy.REVERSE_BITS,
                Strategy.REVERSE_BITS | Strategy.BIG_ENDIAN};
        Byte[] word = new Byte[4];
        for (int strategy : strategies) {
            writeInt(0xDEADBEEF, word, strategy);
            assertEquals(0xDEADBEEF, readInt(word, strategy));
        }
    }

    @Test
    public void testNativeBytesToBytes() {
        assertArrayEquals(new Byte[]{0, 1, -1}, nativeBytesToBytes(new byte[]{0, 1, -1}));
        assertArrayEquals(new Byte[0], nativeBytesToBytes(new byte[0]));
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
