/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.helpers;

import net.jcip.annotations.NotThreadSafe;

import java.math.BigInteger;
import java.util.ArrayList;
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

    /**
     * Maximum number of digits that fit in a long for each radix (index).
     * Used as a heuristic to decide whether Long.parseUnsignedLong can be used.
     * Index 0 and 1 are unused. Computed conservatively (values from Long.parseUnsignedLong source).
     */
    private static final int[] MAX_LONG_DIGITS = new int[Character.MAX_RADIX + 1];

    static {
        // Precompute max digit counts per radix for unsigned long
        // These are the maximum string lengths that Long.parseUnsignedLong can handle
        for (int radix = Character.MIN_RADIX; radix <= Character.MAX_RADIX; radix++) {
            // BigInteger.valueOf(Long.MAX_VALUE) * 2 + 1 = 2^64 - 1 max digits
            // We use a conservative bound: floor(64 / log2(radix))
            // but the simplest correct approach is to compute it
            MAX_LONG_DIGITS[radix] = Long.toUnsignedString(-1, radix).length();
        }
    }

    private final List<NumberPattern> patterns = new ArrayList<>();

    /**
     * This class represents a number pattern in single radix
     */
    public static class NumberPattern {
        private final Pattern pattern;
        private final int radix;
        private final int start;
        private final int end;
        private Matcher cachedMatcher;

        /**
         * Create instance of the NumberPattern
         *
         * @param regex        Regular expression for the number parser
         * @param radix        The radix that the pattern represents
         * @param cutFromStart Count of characters that will be cut from the beginning of a number
         *                     by calling <code>prepareNumber</code> method.
         * @param cutFromEnd   Count of characters that will be cut from the end of a number by
         *                     calling <code>prepareNumber</code> method.
         */
        public NumberPattern(String regex, int radix, int cutFromStart, int cutFromEnd) {
            pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            this.radix = radix;
            this.start = cutFromStart;
            this.end = cutFromEnd;
        }

        /**
         * Determines if a number represented as String matches this
         * NumberPattern.
         *
         * @param number the number String representation
         * @return true if the number matches this pattern, false otherwise
         */
        public boolean matches(String number) {
            if (cachedMatcher == null) {
                cachedMatcher = pattern.matcher(number);
            } else {
                cachedMatcher.reset(number);
            }
            return cachedMatcher.matches();
        }

        /**
         * Get radix of this NumberPattern
         *
         * @return radix of this pattern
         */
        public int getRadix() {
            return radix;
        }

        /**
         * Prepares the number for radix conversion.
         * <p>
         * It formats given number into a form that does not contain any
         * additional characters for radix recognition.
         * <p>
         * For example, pattern <pre>0x[0-9a-fA-F]+</pre>, representing
         * hexadecimal number, contains some characters needed for pattern
         * recognition, they are the first two (<pre>0x</pre>).
         * <p>
         * The numbers of left and right cut are defined in the constructor.
         *
         * @param number the number String representation
         * @return String number prepared for radix conversion.
         */
        public String prepareNumber(String number) {
            return number.substring(start, number.length() - end);
        }
    }

    private RadixUtils() {
        initializeDefaultPatterns();
    }

    private void initializeDefaultPatterns() {
        patterns.add(new NumberPattern("0x[0-9a-f]+", 16, 2, 0));
        patterns.add(new NumberPattern("[0-9a-f]+h", 16, 0, 1));
        patterns.add(new NumberPattern("[0-9]+", 10, 0, 0));
        patterns.add(new NumberPattern("[0-9]+d", 10, 0, 1));
        patterns.add(new NumberPattern("[0-7]+o", 8, 0, 1));
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
     * Clears all user-defined patterns
     */
    public void setDefaults() {
        patterns.clear();
        initializeDefaultPatterns();
    }

    /**
     * Add NumberPattern for new automatic radix recognition
     *
     * @param pattern NumberPattern instance
     */
    public void addNumberPattern(NumberPattern pattern) {
        patterns.add(pattern);
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
     * Converts number in any length to a number with specified radix.
     * <p>
     * This method will automatically detect the number original radix. It
     * can detect several hexadecimal, decimal, and octal formats.
     *
     * @param number  String representing number in hexa, octal or decadic radix
     * @param toRadix target radix of the number
     * @return String of a number in specified radix
     * @throws NumberFormatException if the number is not in known format
     */
    public String convertToRadix(String number, int toRadix) {
        for (NumberPattern pattern : patterns) {
            if (pattern.matches(number)) {
                if (pattern.getRadix() == toRadix) {
                    return pattern.prepareNumber(number);
                }
                return convertToRadix(pattern.prepareNumber(number), pattern.getRadix(), toRadix);
            }
        }
        throw new NumberFormatException("Number not recognized");
    }

    /**
     * Converts number in any length to a number with specified radix.
     *
     * @param number    String representing number in any radix
     * @param fromRadix source radix of the number
     * @param toRadix   target radix of the number
     * @return String of a number in target radix
     */
    public static String convertToRadix(String number, int fromRadix, int toRadix) {
        if (fromRadix == toRadix) {
            return number;
        }
        checkRadix(fromRadix);
        checkRadix(toRadix);
        // Fast path using long
        if (number.length() <= MAX_LONG_DIGITS[fromRadix]) {
            try {
                long value = Long.parseUnsignedLong(number, fromRadix);
                return toRadixStringFast(value, toRadix);
            } catch (NumberFormatException e) {
                // fall through to BigInteger path
            }
        }
        return toRadixStringUnchecked(parseUnsignedBigIntegerUnchecked(number, fromRadix), toRadix);
    }

    /**
     * Convert an integer number in any radix (stored in String) to binary
     * components (bytes) in little endian.
     * <p>
     * Complexity: O(n)
     *
     * @param number    number stored as String
     * @param fromRadix the radix of the number
     * @return Array of binary components of that number
     */
    public static byte[] convertToNumber(String number, int fromRadix) {
        checkRadix(fromRadix);
        // Fast path: try long first
        if (number.length() <= MAX_LONG_DIGITS[fromRadix]) {
            try {
                long value = Long.parseUnsignedLong(number, fromRadix);
                if (value == 0) {
                    return new byte[]{0};
                }
                return longToLittleEndianBytes(value, -1);
            } catch (NumberFormatException e) {
                // fall through to BigInteger path
            }
        }
        BigInteger parsed = parseUnsignedBigIntegerUnchecked(number, fromRadix);
        if (parsed.signum() == 0) {
            return new byte[]{0};
        }
        return toLittleEndianBytes(parsed.toByteArray(), -1);
    }

    /**
     * Convert an integer number in any radix (stored in String) to binary
     * components (bytes) in little endian.
     * <p>
     * Complexity: O(n)
     *
     * @param number     number stored as String
     * @param fromRadix  the radix of the number
     * @param bytesCount number of bytes. If the results has fewer bytes, they will be appended from the left. If
     *                   it contains more bytes, they will be cut from the left (from MSB).
     * @return Array of binary components of that number
     */
    public static byte[] convertToNumber(String number, int fromRadix, int bytesCount) {
        checkRadix(fromRadix);
        // Fast path: try long first
        if (number.length() <= MAX_LONG_DIGITS[fromRadix]) {
            try {
                long value = Long.parseUnsignedLong(number, fromRadix);
                return longToLittleEndianBytes(value, bytesCount);
            } catch (NumberFormatException e) {
                // fall through to BigInteger path
            }
        }
        return toLittleEndianBytes(parseUnsignedBigIntegerUnchecked(number, fromRadix).toByteArray(), bytesCount);
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
                return Integer.parseInt(pattern.prepareNumber(number), pattern.getRadix());
            }
        }
        throw new NumberFormatException("Number not recognized");
    }

    /**
     * Parses a number in known radix into integer.
     *
     * @param number number in some known radix
     * @param radix  radix of the number (known pattern must exist for parsing)
     * @return parsed integer
     * @throws NumberFormatException if there is no pattern available for given radix or the number is unparseable
     */
    public int parseRadix(String number, int radix) throws NumberFormatException {
        for (NumberPattern pattern : patterns) {
            if (pattern.getRadix() == radix && pattern.matches(number)) {
                return Integer.parseInt(pattern.prepareNumber(number), radix);
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

    private static BigInteger parseUnsignedBigIntegerUnchecked(String number, int radix) {
        BigInteger parsed = new BigInteger(number, radix);
        if (parsed.signum() < 0) {
            throw new NumberFormatException("Too big number to parse");
        }
        return parsed;
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

    /**
     * Convert a long value to little-endian byte array.
     *
     * @param value      the long value
     * @param bytesCount target byte count, or -1 for minimal representation
     */
    private static byte[] longToLittleEndianBytes(long value, int bytesCount) {
        if (bytesCount >= 0) {
            byte[] result = new byte[bytesCount];
            for (int i = 0; i < bytesCount && value != 0; i++) {
                result[i] = (byte) (value & 0xFF);
                value >>>= 8;
            }
            return result;
        }
        // Minimal representation
        if (value == 0) {
            return new byte[]{0};
        }
        int len = (64 - Long.numberOfLeadingZeros(value) + 7) >>> 3;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++) {
            result[i] = (byte) (value & 0xFF);
            value >>>= 8;
        }
        return result;
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

    private static byte[] toLittleEndianBytes(byte[] bigEndianBytes, int bytesCount) {
        int offset = (bigEndianBytes.length > 1 && bigEndianBytes[0] == 0) ? 1 : 0;
        int magnitudeLength = bigEndianBytes.length - offset;

        if (bytesCount >= 0) {
            byte[] result = new byte[bytesCount];
            int copyLength = Math.min(magnitudeLength, bytesCount);
            for (int i = 0; i < copyLength; i++) {
                result[i] = bigEndianBytes[bigEndianBytes.length - 1 - i];
            }
            return result;
        }

        // Special case for 1 byte - avoid loop
        if (magnitudeLength == 1) {
            return new byte[]{bigEndianBytes[offset]};
        }

        byte[] result = new byte[magnitudeLength];
        for (int i = 0; i < magnitudeLength; i++) {
            result[i] = bigEndianBytes[bigEndianBytes.length - 1 - i];
        }
        return result;
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
