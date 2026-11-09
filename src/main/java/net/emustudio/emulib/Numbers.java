/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib;

import net.jcip.annotations.ThreadSafe;

/**
 * Parsing, formatting, bit, byte-order and BCD operations on numbers and memory cells.
 */
@ThreadSafe
public final class Numbers {
    private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();
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

    private Numbers() {
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

    /**
     * Parses an integer written in assembler-style notation.
     * <p>
     * Accepted syntax (case-insensitive, no surrounding whitespace): optional sign {@code +}/{@code -}, followed by
     * <ul>
     *   <li>{@code 0x} prefix: hexadecimal ({@code 0x1F})</li>
     *   <li>{@code h} suffix: hexadecimal ({@code 1Fh})</li>
     *   <li>{@code b} suffix or {@code 0b} prefix: binary ({@code 101b}, {@code 0b101})</li>
     *   <li>{@code o} or {@code q} suffix: octal ({@code 17o}, {@code 17q})</li>
     *   <li>{@code d} suffix or no affix: decimal ({@code 12d}, {@code 12}; leading zeros are decimal too)</li>
     * </ul>
     * The result must fit into a signed 32-bit integer.
     *
     * @param number the number text
     * @return parsed value
     * @throws NumberFormatException if the text is not a number in a recognized format, or it overflows
     */
    public static int parseRadix(String number) throws NumberFormatException {
        int start = 0;
        int end = number.length();
        boolean negative = false;
        if (end > 0 && (number.charAt(0) == '-' || number.charAt(0) == '+')) {
            negative = number.charAt(0) == '-';
            start++;
        }

        int radix;
        if (hasPrefix(number, start, 'x')) {
            radix = 16;
            start += 2;
        } else {
            radix = end > start ? suffixRadix(number.charAt(end - 1)) : 0;
            if (radix != 0) {
                end--;
            } else if (hasPrefix(number, start, 'b')) {
                radix = 2;
                start += 2;
            } else {
                radix = 10;
            }
        }
        if (start >= end) {
            throw new NumberFormatException("Number not recognized: " + number);
        }

        long value = 0;
        for (int i = start; i < end; i++) {
            int digit = digit(number.charAt(i));
            if (digit >= radix) {
                throw new NumberFormatException("Number not recognized: " + number);
            }
            value = value * radix + digit;
            if (value > 0x80000000L) {
                throw new NumberFormatException("Number out of range: " + number);
            }
        }
        if (negative) {
            value = -value;
        } else if (value > Integer.MAX_VALUE) {
            throw new NumberFormatException("Number out of range: " + number);
        }
        return (int) value;
    }

    /**
     * Formats a value as an uppercase hexadecimal number, left-padded with zeros.
     * <p>
     * The result is never truncated: if the value needs more digits, all are emitted (negative values have 8 digits).
     *
     * @param value  the value
     * @param digits minimal number of digits (e.g. 2 for a byte, 4 for a word, 8 for a dword)
     * @return hexadecimal string without prefix or suffix
     */
    public static String formatHex(int value, int digits) {
        int needed = (35 - Integer.numberOfLeadingZeros(value | 1)) >>> 2;
        char[] result = new char[Math.max(digits, needed)];
        for (int i = result.length - 1; i >= 0; i--) {
            result[i] = HEX_DIGITS[value & 0xF];
            value >>>= 4;
        }
        return new String(result);
    }

    /**
     * Formats a value as a binary number, left-padded with zeros.
     * <p>
     * The result is never truncated: if the value needs more bits, all are emitted (negative values have 32 bits).
     *
     * @param value the value
     * @param bits  minimal number of bits
     * @return binary string without prefix or suffix
     */
    public static String formatBinary(int value, int bits) {
        return formatBinary(value, bits, 0);
    }

    /**
     * Formats a value as a binary number, left-padded with zeros, with bit groups separated by a space.
     * <p>
     * Groups are counted from the least significant bit, e.g. {@code formatBinary(0x2F, 10, 4)} is
     * {@code "00 0010 1111"}. The result is never truncated (negative values have 32 bits).
     *
     * @param value     the value
     * @param bits      minimal number of bits
     * @param groupBits number of bits in a space-separated group; if &lt;= 0, bits are not grouped
     * @return binary string without prefix or suffix
     */
    public static String formatBinary(int value, int bits, int groupBits) {
        int total = Math.max(bits, 32 - Integer.numberOfLeadingZeros(value | 1));
        int spaces = groupBits > 0 ? (total - 1) / groupBits : 0;
        char[] result = new char[total + spaces];
        int index = result.length - 1;
        for (int bit = 0; bit < total; bit++) {
            if (bit > 0 && groupBits > 0 && bit % groupBits == 0) {
                result[index--] = ' ';
            }
            result[index--] = (bit < 32 && ((value >>> bit) & 1) != 0) ? '1' : '0';
        }
        return new String(result);
    }

    private static boolean hasPrefix(String number, int start, char radixLetter) {
        return number.length() > start + 2 && number.charAt(start) == '0'
                && Character.toLowerCase(number.charAt(start + 1)) == radixLetter;
    }

    private static int suffixRadix(char suffix) {
        switch (Character.toLowerCase(suffix)) {
            case 'h': return 16;
            case 'b': return 2;
            case 'o': case 'q': return 8;
            case 'd': return 10;
            default: return 0;
        }
    }

    private static int digit(char c) {
        if (c >= '0' && c <= '9') return c - '0';
        if (c >= 'a' && c <= 'f') return c - 'a' + 10;
        if (c >= 'A' && c <= 'F') return c - 'A' + 10;
        return Integer.MAX_VALUE;
    }
}
