/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.helpers;

import net.jcip.annotations.ThreadSafe;

/**
 * Parsing and formatting of numbers in the radixes common in system programming.
 */
@ThreadSafe
public final class RadixUtils {
    private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();

    private RadixUtils() {
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
