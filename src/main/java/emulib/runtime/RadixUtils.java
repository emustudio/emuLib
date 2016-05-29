/*
 * KISS, YAGNI, DRY
 *
 * (c) Copyright 2006-2016, Peter Jakubčo
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package emulib.runtime;

import net.jcip.annotations.NotThreadSafe;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * The class contains several methods used for work with numbers in various
 * radixes.
 *
 * Numbers represented in various radixes are used widely in system programming
 * in all times. This class tries to make parsing, converting and working with
 * various number radixes easier.
 *
 */
@NotThreadSafe
public class RadixUtils {
    private static final double LOG102 = 0.30102999566398114;
    private static final RadixUtils INSTANCE = new RadixUtils();

    private final List<NumberPattern> patterns = new ArrayList<>();

    /**
     * This class represents a number pattern in single radix
     */
    public static class NumberPattern {
        private final Pattern pattern;
        private final int radix;
        private final int start;
        private final int end;

        /**
         * Create instance of the NumberPattern
         *
         * @param regex Regular expression for the number parser
         * @param radix The radix that the pattern represents
         * @param cutFromStart
         *   Count of characters that will be cut from the beginning of a number
         *   by calling <code>prepareNumber</code> method.
         * @param cutFromEnd
         *   Count of characters that will be cut from the end of a number by
         *   calling <code>prepareNumber</code> method.
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
            return pattern.matcher(number).matches();
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
         *
         * It formats given number into a form that does not contain any
         * additional characters for radix recognition.
         *
         * For example, pattern <pre>0x[0-9a-fA-F]+</pre>, representing
         * hexadecimal number, contains some characters needed for pattern
         * recognition, they are the first two (<pre>0x</pre>).
         *
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
        patterns.add(new NumberPattern("0[0-9]+", 8, 1, 0));
        patterns.add(new NumberPattern("[0-9]+o", 8, 0, 1));
    }

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
     * Add NumberPattern for further automatic radix recognition
     *
     * @param pattern NumberPattern instance
     */
    public void addNumberPattern(NumberPattern pattern) {
        patterns.add(pattern);
    }

    /**
     * Converts number in any length to a number with specified radix.
     *
     * @param number any-length number. Array of number components stored in
     *               little endian.
     * @param toRadix the radix of converted number
     * @param littleEndian If the number is in little endian (true), or big endian (false)
     *
     * @return String of a number in specified radix
     */
    public static String convertToRadix(byte[] number, int toRadix, boolean littleEndian) {
        int bytes;
        int i, j, val;
        int temp;
        int rem;
        int ip;
        char k;
        String result = "";

        int digitsCount = number.length;

        bytes = (int)Math.ceil((double) digitsCount
                * 8.0 *  LOG102 / Math.log10(toRadix)) + 2;

        if (!littleEndian) {
            for (i = 0; i < digitsCount / 2; i++) {
                byte tmp = number[i];
                number[i] = number[digitsCount - i - 1];
                number[digitsCount - i - 1] = tmp;
            }
        }

        int[] str = new int[bytes+1];
        int[] ts = new int[bytes+1];

        ts[0] = 1;
        for (k = 0; k < digitsCount; k++) {
            short digit = number[k];

            for (i = 0; i < 8; i++) {
                val = (digit >>> i) & 1;
                for (j = 0; j < bytes; j++) {
                    str[j] += ts[j] * val;
                    temp = str[j];
                    ip = j;
                    do { // fix up any remainders in radix
                        rem = temp / toRadix;
                        str[ip++] = temp - rem * toRadix;
                        str[ip] += rem;
                        temp = str[ip];
                    } while (temp >= toRadix);
                }

                //calculate the next power 2^i in radix format
                for (j = 0; j < bytes; j++) {
                    ts[j] = ts[j] * 2;
                }
                for (j = 0; j < bytes; j++) { //check for any remainders
                    temp = ts[j];
                    ip = j;
                    do { //fix up any remainders
                        rem = temp / toRadix;
                        ts[ip++] = temp - rem * toRadix;
                        ts[ip] += rem;
                        temp = ts[ip];
                    } while (temp >= toRadix);
                }
            }
        }

        //convert the output to string format (digits 0,to-1 converted to 0-Z
        //characters)
        boolean first = false; //leading zero flag
        for (i = bytes - 1; i >= 0; i--) {
            if (str[i] != 0) {
                first = true;
            }
            if (!first) {
                continue;
            }
            if (str[i] < 10) {
                result += (char)(str[i] + (int)'0');
            } else {
                result += (char)(str[i] + (int)'A' - 10);
            }
        }
        if (!first) {
            result += "0";
        }
        return result;
    }

    /**
     * Converts number in any length to a number with specified radix.
     *
     * This method will automatically detect the number original radix. It
     * can detect several hexadecimal, decimal, and octal formats.
     *
     * @param number String representing number in hexa, octal or decadic radix
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
                return convertToRadix(
                        convertToNumber(pattern.prepareNumber(number), pattern.getRadix()), toRadix, true
                );
            }
        }
        throw new NumberFormatException("Number not recognized");
    }

    /**
     * Converts number in any length to a number with specified radix.
     *
     * @param number String representing number in any radix
     * @param fromRadix source radix of the number
     * @param toRadix target radix of the number
     * @return String of a number in target radix
     */
    public static String convertToRadix(String number, int fromRadix, int toRadix) {
        if (fromRadix == toRadix) {
            return number;
        }
        byte[] xnumber = convertToNumber(number, fromRadix);
        return convertToRadix(xnumber, toRadix, true);
    }

    /**
     * Convert a integer number in some radix (stored in String) to binary
     * components in little endian.
     *
     * Complexity: O(n^2)
     *
     * @param number number stored as String
     * @param fromRadix the radix of the number
     * @return Array of binary components of that number
     */
    public static byte[] convertToNumber(String number, int fromRadix) {
        List<Byte> bytes = new ArrayList<>();

        long parsed = Long.parseLong(number, fromRadix);
        if (parsed < 0) {
            throw new NumberFormatException("Too big number to parse");
        }
        while (parsed != 0) {
            bytes.add((byte)(parsed & 0xFF));
            parsed >>>= 8;
        }

        byte[] result = new byte[bytes.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = bytes.get(i);
        }

        return result;
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
     * @param radix radix of the number (known pattern must exist for parsing)
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
     *
     * Formatting is using pattern "%02X".
     *
     * @param byteNumber a number, assumed size is a byte
     * @return formatted string as a hexadecimal number, with string length=2
     */
    public static String getByteHexString(int byteNumber) {
        return String.format("%02X", byteNumber);
    }

    /**
     * Get formatted string of a word.
     *
     * Formatting is using pattern "%04X".
     *
     * @param wordNumber a number, assumed size is a word (2 bytes)
     * @return formatted string as a hexadecimal number, with string length=4
     */
    public static String getWordHexString(int wordNumber) {
        return String.format("%04X", wordNumber);
    }

    /**
     * Get formatted string of a word.
     *
     * Formatting is using pattern "%04X".
     *
     * @param upper high order byte (high 8 bits)
     * @param lower low order byte (low 8bits)
     * @return formatted string as a hexadecimal number, with string length=4
     */
    public static String getWordHexString(short upper, short lower) {
        return String.format("%04X", ((upper << 8) | lower) & 0xFFFF);
    }

    /**
     * Get formatted string of a dword.
     *
     * Formatting is using pattern "%08X".
     *
     * @param number a number, assumed size is a double word (4 bytes)
     * @return formatted string as a hexadecimal number, with string length=8
     */
    public static String getDwordHexString(int number) {
        return String.format("%08X", number);
    }

}
