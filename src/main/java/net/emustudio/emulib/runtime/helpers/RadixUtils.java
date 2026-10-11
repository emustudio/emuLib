/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.helpers;

import net.jcip.annotations.NotThreadSafe;

import java.math.BigInteger;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The class contains several methods used for work with numbers in various
 * radixes.
 * <p>
 * Numbers represented in various radixes are used widely in system programming
 * in all times. This class tries to make parsing, converting and working with
 * various number radixes easier.
 */
@NotThreadSafe
public class RadixUtils {
    private static final RadixUtils INSTANCE = new RadixUtils();
    private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();

    private final List<NumberPattern> patterns = List.of(
            new NumberPattern("0x[0-9a-f]+", 16, 2, 0),
            new NumberPattern("[0-9a-f]+h", 16, 0, 1),
            new NumberPattern("[0-9]+", 10, 0, 0),
            new NumberPattern("[0-9]+d", 10, 0, 1),
            new NumberPattern("[0-7]+o", 8, 0, 1)
    );

    // Number syntax in one radix; prefix and suffix characters (e.g. "0x", "h") are cut before parsing.
    private static final class NumberPattern {
        private final Pattern pattern;
        private final int radix;
        private final int prefixLength;
        private final int suffixLength;
        private Matcher cachedMatcher;

        NumberPattern(String regex, int radix, int prefixLength, int suffixLength) {
            this.pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            this.radix = radix;
            this.prefixLength = prefixLength;
            this.suffixLength = suffixLength;
        }

        boolean matches(String number) {
            if (cachedMatcher == null) {
                cachedMatcher = pattern.matcher(number);
            } else {
                cachedMatcher.reset(number);
            }
            return cachedMatcher.matches();
        }

        int parse(String number) {
            return Integer.parseInt(number.substring(prefixLength, number.length() - suffixLength), radix);
        }
    }

    private RadixUtils() {
    }

    /**
     * Gets the singleton instance of RadixUtils.
     *
     * @return RadixUtils instance
     */
    public static RadixUtils getInstance() {
        return INSTANCE;
    }

    /**
     * Converts number in any length to a number with specified radix.
     *
     * @param number       any-length number. Array of number components stored in
     *                     little endian.
     * @param toRadix      the radix of converted number
     * @param littleEndian If the number is in little endian (true), or big endian (false)
     * @return String of a number in specified radix
     */
    public static String convertToRadix(byte[] number, int toRadix, boolean littleEndian) {
        checkRadix(toRadix);
        if (toRadix == 16) {
            return toHexString(number, littleEndian);
        }
        // Fast path: if the byte array fits in a long (≤ 8 bytes), avoid BigInteger
        if (number.length <= 8) {
            long value = toLongValue(number, littleEndian);
            return toRadixStringFast(value, toRadix);
        }
        return toRadixStringUnchecked(toBigInteger(number, littleEndian), toRadix);
    }

    /**
     * Parses a number in known radix into integer.
     *
     * @param number number in some known radix
     * @return parsed integer
     * @throws NumberFormatException if the number is not in known format
     */
    public int parseRadix(String number) throws NumberFormatException {
        for (NumberPattern pattern : patterns) {
            if (pattern.matches(number)) {
                return pattern.parse(number);
            }
        }
        throw new NumberFormatException("Number not recognized");
    }

    /**
     * Get formatted string of a byte.
     * <p>
     * Formatting is using pattern "%02X".
     *
     * @param byteNumber a number, assumed size is a byte
     * @return formatted string as a hexadecimal number, with string length=2
     */
    public static String formatByteHexString(int byteNumber) {
        return formatHexString(byteNumber, 2);
    }

    /**
     * Get formatted string of a word.
     * <p>
     * Formatting is using pattern "%04X".
     *
     * @param wordNumber a number, assumed size is a word (2 bytes)
     * @return formatted string as a hexadecimal number, with string length=4
     */
    public static String formatWordHexString(int wordNumber) {
        return formatHexString(wordNumber, 4);
    }

    /**
     * Get formatted string of a word.
     * <p>
     * Formatting is using pattern "%04X".
     *
     * @param upper high order byte (high 8 bits)
     * @param lower low order byte (low 8bits)
     * @return formatted string as a hexadecimal number, with string length=4
     */
    public static String formatWordHexString(short upper, short lower) {
        return formatHexString(((upper & 0xFF) << 8) | (lower & 0xFF), 4);
    }

    /**
     * Get formatted string of a dword.
     * <p>
     * Formatting is using pattern "%08X".
     *
     * @param number a number, assumed size is a double word (4 bytes)
     * @return formatted string as a hexadecimal number, with string length=8
     */
    public static String formatDwordHexString(int number) {
        return formatHexString(number, 8);
    }

    /**
     * Get formatted binary string of given number.
     * <p>
     * The formatted string is possibly prepended with zeroes to ensure that the string has given length.
     * <p>
     * Also, groups of some bits can be separated by single space. The number of space-separated bits is specified
     * by the `spacesPerBits` parameter.
     *
     * @param number         number to format
     * @param length         resulting string length (number of bits)
     * @param spacePerBits   number of space-separated bits. If &lt;= 0 then bits are never separated with space.
     * @param spacesFromLeft whether the group of bits to be space-separated should be counted from left or from right side
     * @return formatted string as a binary number, with given string length
     */
    public static String formatBinaryString(int number, int length, int spacePerBits, boolean spacesFromLeft) {
        int totalBits = Math.max(length, bitLength(number));
        int spaces = (spacePerBits > 0) ? ((totalBits - 1) / spacePerBits) : 0;
        char[] result = new char[totalBits + spaces];
        int index = 0;

        for (int bit = 0; bit < totalBits; bit++) {
            if (spacePerBits > 0) {
                if (spacesFromLeft) {
                    if (bit > 0 && (bit % spacePerBits) == 0) {
                        result[index++] = ' ';
                    }
                } else if (bit > 0 && ((totalBits - bit) % spacePerBits) == 0) {
                    result[index++] = ' ';
                }
            }

            int shift = totalBits - bit - 1;
            result[index++] = (shift < Integer.SIZE && ((number >>> shift) & 1) != 0) ? '1' : '0';
        }

        return new String(result);
    }

    /**
     * Get formatted binary string of given number.
     * <p>
     * The formatted string is possibly prepended with zeroes to ensure that the string has given length.
     * <p>
     * Bits are not separated by spaces.
     *
     * @param number number to format
     * @param length resulting string length (number of bits)
     * @return formatted string as a binary number, with given string length
     */
    public static String formatBinaryString(int number, int length) {
        return formatBinaryString(number, length, 0, false);
    }

    /**
     * Convert a long value to a radix string, uppercasing a-f in-place (no extra String allocation).
     */
    private static String toRadixStringFast(long value, int radix) {
        String result = Long.toUnsignedString(value, radix);
        if (radix <= 10) {
            return result;
        }
        // Uppercase in-place to avoid toUpperCase() allocation
        char[] chars = result.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c >= 'a' && c <= 'z') {
                chars[i] = (char) (c - 32);
            }
        }
        return new String(chars);
    }

    /**
     * Convert BigInteger to radix string without checking radix (caller must have checked).
     */
    private static String toRadixStringUnchecked(BigInteger value, int radix) {
        String result = value.toString(radix);
        if (radix <= 10) {
            return result;
        }
        // Uppercase in-place to avoid toUpperCase(Locale.ROOT) allocation
        char[] chars = result.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c >= 'a' && c <= 'z') {
                chars[i] = (char) (c - 32);
            }
        }
        return new String(chars);
    }

    /**
     * Convert a byte array to a long value. Array must be ≤ 8 bytes.
     */
    private static long toLongValue(byte[] number, boolean littleEndian) {
        long value = 0;
        if (littleEndian) {
            for (int i = number.length - 1; i >= 0; i--) {
                value = (value << 8) | (number[i] & 0xFFL);
            }
        } else {
            for (byte b : number) {
                value = (value << 8) | (b & 0xFFL);
            }
        }
        return value;
    }

    private static BigInteger toBigInteger(byte[] number, boolean littleEndian) {
        if (number.length == 0) {
            return BigInteger.ZERO;
        }
        if (!littleEndian) {
            return new BigInteger(1, number);
        }

        byte[] magnitude = new byte[number.length];
        for (int i = 0, j = number.length - 1; i < number.length; i++, j--) {
            magnitude[j] = number[i];
        }
        return new BigInteger(1, magnitude);
    }

    private static String toHexString(byte[] number, boolean littleEndian) {
        int start = littleEndian ? number.length - 1 : 0;
        int end = littleEndian ? -1 : number.length;
        int step = littleEndian ? -1 : 1;

        while (start != end && number[start] == 0) {
            start += step;
        }
        if (start == end) {
            return "0";
        }

        int significantBytes = littleEndian ? (start + 1) : (number.length - start);
        int mostSignificantByte = number[start] & 0xFF;
        char[] result = new char[(significantBytes << 1) - ((mostSignificantByte >>> 4) == 0 ? 1 : 0)];

        int index = appendHexByte(result, 0, mostSignificantByte, true);
        for (int i = start + step; i != end; i += step) {
            index = appendHexByte(result, index, number[i] & 0xFF, false);
        }
        return new String(result);
    }

    private static int appendHexByte(char[] result, int index, int value, boolean trimLeadingNibble) {
        int upperNibble = (value >>> 4) & 0xF;
        if (!trimLeadingNibble || upperNibble != 0) {
            result[index++] = HEX_DIGITS[upperNibble];
        }
        result[index++] = HEX_DIGITS[value & 0xF];
        return index;
    }

    private static String formatHexString(int value, int digits) {
        char[] result = new char[Math.max(digits, hexLength(value))];
        for (int i = result.length - 1; i >= 0; i--) {
            result[i] = HEX_DIGITS[value & 0xF];
            value >>>= 4;
        }
        return new String(result);
    }

    private static int hexLength(int value) {
        if (value == 0) {
            return 1;
        }
        if (value < 0) {
            return Integer.SIZE / 4;
        }
        return (bitLength(value) + 3) / 4;
    }

    private static int bitLength(int number) {
        if (number == 0) {
            return 1;
        }
        return 32 - Integer.numberOfLeadingZeros(number);
    }


    private static void checkRadix(int radix) {
        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) {
            throw new NumberFormatException("Radix out of range");
        }
    }
}
