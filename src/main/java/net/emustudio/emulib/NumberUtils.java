/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib;

import net.jcip.annotations.ThreadSafe;

/**
 * Bit, byte-order and BCD operations on numbers and memory cells.
 */
@ThreadSafe
public final class NumberUtils {
    private static final int[] REVERSED_BYTES = new int[256];
    private static final int[] BCD_TO_BIN = new int[256];
    private static final int[] BIN_TO_BCD = new int[100];

    static {
        for (int i = 0; i < 256; i++) {
            REVERSED_BYTES[i] = Integer.reverse(i) >>> 24;
            BCD_TO_BIN[i] = (i >> 4) * 10 + (i & 0xF);
        }
        for (int i = 0; i < 100; i++) {
            BIN_TO_BCD[i] = ((i / 10) << 4) | (i % 10);
        }
    }

    private NumberUtils() {
    }

    /**
     * Strategy defining how bytes are combined into a number. Strategies can be combined with | (or) operator.
     */
    public static final class Strategy {
        private Strategy() {
        }

        /**
         * Bytes are read/written in the little endian (the default, if BIG_ENDIAN is not set)
         */
        public final static int LITTLE_ENDIAN = 1;

        /**
         * Bytes are read/written in the big endian
         */
        public final static int BIG_ENDIAN = 2;

        /**
         * Bits in particular bytes are reversed
         */
        public final static int REVERSE_BITS = 4;
    }

    /**
     * Reverse bits in integer (max 32-bit) value.
     *
     * @param value        value which bits will be reversed
     * @param numberOfBits how many bits should be reversed. If the value has more bits, the rest will be preserved.
     * @return value with reversed bits
     */
    public static int reverseBits(int value, int numberOfBits) {
        if (numberOfBits <= 0) {
            return value;
        }
        if (numberOfBits >= 32) {
            return Integer.reverse(value);
        }
        int mask = -1 >>> (32 - numberOfBits);
        return (value & ~mask) | (Integer.reverse(value & mask) >>> (32 - numberOfBits));
    }

    /**
     * Reverse bits in long (max 64-bit) value.
     * <p>
     * Unlike {@link #reverseBits(int, int)}, higher bits of {@code value} are discarded (not preserved), and
     * {@code numberOfBits <= 0} yields {@code 0}.
     *
     * @param value        value which bits will be reversed
     * @param numberOfBits how many lowest bits should be reversed
     * @return value with the lowest {@code numberOfBits} bits reversed and higher bits cleared
     */
    public static long reverseBits(long value, int numberOfBits) {
        if (numberOfBits <= 0) {
            return 0;
        }
        if (numberOfBits >= 64) {
            return Long.reverse(value);
        }
        return Long.reverse(value & (-1L >>> (64 - numberOfBits))) >>> (64 - numberOfBits);
    }

    /**
     * Reads an arbitrary number of bits (max 32) from bytes.
     * <p>
     * If the strategy is LITTLE_ENDIAN, bits are read from LSB to MSB. If it is BIG_ENDIAN, bits are read from MSB
     * to LSB.
     *
     * @param bytes    bytes
     * @param start    the bit index where reading starts
     * @param length   the number of bits to read
     * @param strategy combination of {@link Strategy} flags
     * @return the bits read
     */
    public static int readBits(byte[] bytes, int start, int length, int strategy) {
        int startByte = start >>> 3;
        int endByte = (start + length - 1) >>> 3;
        boolean bigEndian = (strategy & Strategy.BIG_ENDIAN) != 0;
        boolean reverse = (strategy & Strategy.REVERSE_BITS) != 0;

        int value = 0;
        for (int i = 0; i <= endByte - startByte; i++) {
            int cell = bytes[bigEndian ? startByte + i : endByte - i] & 0xFF;
            value = (value << 8) | (reverse ? REVERSED_BYTES[cell] : cell);
        }
        int shift = bigEndian ? (8 - ((start + length) & 7)) & 7 : start & 7;
        int clear = length >= 32 ? -1 : (1 << length) - 1;
        return (value >>> shift) & clear;
    }

    /**
     * Combines up to 4 memory cells into an integer.
     * <p>
     * Each cell represents one byte; higher-order bits of a cell are cut. If there are more than 4 cells, only the
     * first 4 are used. If there are fewer, the missing most significant bytes are zero.
     *
     * @param word     the cells (e.g. {@code Byte[]}, {@code Short[]} or {@code Integer[]})
     * @param strategy combination of {@link Strategy} flags
     * @return 32-bit value
     */
    public static int readInt(Number[] word, int strategy) {
        boolean bigEndian = (strategy & Strategy.BIG_ENDIAN) != 0;
        boolean reverse = (strategy & Strategy.REVERSE_BITS) != 0;
        int length = Math.min(4, word.length);

        int value = 0;
        for (int i = 0; i < length; i++) {
            int cell = word[bigEndian ? i : length - 1 - i].intValue() & 0xFF;
            value = (value << 8) | (reverse ? REVERSED_BYTES[cell] : cell);
        }
        return value;
    }

    /**
     * Splits an integer into 4 bytes. It is the inverse of {@link #readInt(Number[], int)}.
     *
     * @param value    the value
     * @param output   the output; must have space for 4 bytes. Other elements are untouched.
     * @param strategy combination of {@link Strategy} flags
     */
    public static void writeInt(int value, Byte[] output, int strategy) {
        boolean bigEndian = (strategy & Strategy.BIG_ENDIAN) != 0;
        boolean reverse = (strategy & Strategy.REVERSE_BITS) != 0;

        for (int i = 0; i < 4; i++) {
            int cell = (value >>> (i * 8)) & 0xFF;
            output[bigEndian ? 3 - i : i] = (byte) (reverse ? REVERSED_BYTES[cell] : cell);
        }
    }

    /**
     * Converts native byte[] array to boxed Byte[] array (e.g. for writing into memory).
     *
     * @param array native byte[] array
     * @return boxed Byte[] array
     */
    public static Byte[] nativeBytesToBytes(byte[] array) {
        Byte[] result = new Byte[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i];
        }
        return result;
    }

    /**
     * Converts packed BCD code (1 byte, 2 BCD digits) to binary.
     *
     * @param bcd packed BCD code
     * @return binary number
     */
    public static int bcd2bin(int bcd) {
        return BCD_TO_BIN[bcd & 0xFF];
    }

    /**
     * Converts a binary number into packed BCD (1 byte, 2 BCD digits).
     *
     * @param bin binary number (0-99)
     * @return number in packed BCD code
     */
    public static int bin2bcd(int bin) {
        return BIN_TO_BCD[bin];
    }
}
