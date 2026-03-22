/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.helpers;

import net.jcip.annotations.ThreadSafe;

import java.util.List;
import java.util.RandomAccess;

/**
 * A number utility class with various useful operations on numbers and number arrays.
 */
@ThreadSafe
public class NumberUtils {
    private static final byte[] REVERSED_BYTES = createReversedBytes();

    /**
     * Constructs a new NumberUtils instance.
     */
    private NumberUtils() {
    }

    /**
     * Strategy defining how to manipulate with bytes.
     * <p>
     * Strategies can be combined with | (or) operator.
     */
    public static final class Strategy {
        /**
         * Constructs a new Strategy instance.
         */
        private Strategy() {
        }

        /**
         * Bytes are read/written in the little endian
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
     *
     * @param value        value which bits will be reversed
     * @param numberOfBits how many bits should be reversed. If the value has more bits, the rest will be ignored.
     * @return value with reversed bits
     */
    public static long reverseBits(long value, int numberOfBits) {
        if (numberOfBits <= 0) {
            return 0;
        }
        if (numberOfBits >= 64) {
            return Long.reverse(value);
        }

        long mask = -1L >>> (64 - numberOfBits);
        return Long.reverse(value & mask) >>> (64 - numberOfBits);
    }

    /**
     * Reads an arbitrary number of bits from bytes.
     * If bytesStrategy is LITTLE_ENDIAN, bits are read from LSB to MSB. If the strategy is BIG_ENDIAN, bits are read
     * from MSB to LSB.
     *
     * @param bytes         bytes
     * @param start         the number of bits from the start of the current instruction
     * @param length        the number of bits to read
     * @param bytesStrategy strategy for how to read bytes
     * @return the bytes read
     */
    public static int readBits(byte[] bytes, int start, int length, int bytesStrategy) {
        // if start >= n * 8, we can skip reading whole bytes, because they will be lost by shifting anyway
        int startByte = start / 8;
        int endByte = (start + length - 1) / 8;

        int div = start / 8; // force java to store integer (drop decimal part)
        int realStart = start - 8 * div; // we just "shifted" bits which were not read

        int value = readInt(bytes, startByte, endByte - startByte + 1, bytesStrategy);

        int clear = (int) ((1L << length) - 1);
        int shift;

        boolean littleEndian = ((bytesStrategy & Strategy.BIG_ENDIAN) != Strategy.BIG_ENDIAN);
        if (littleEndian) {
            // from LSB to MSB
            shift = realStart % 8;
        } else {
            // from MSB to LSB
            //
            // 1. (8-length) tells us how much we should shift to the right, if realStart=0
            //
            // 2. if realStart>0, it means we meed more bits from the right, exactly (8-length-realStart) bits
            //
            // 3. we need ((realStart + length) % 8), because we shift at max 7 bits
            //    (we "dropped" whole bytes if start>8)
            //
            // 4. and finally, we need the last %8 because if ((realStart + length) % 8)=0 we end up with shift=8.
            //    That would be the same as dropping the byte, but we dropped it already (thus we can shift only
            //    max 7 times (0-7), not 8)
            shift = (8 - ((realStart + length) % 8)) % 8;
        }

        return (value >>> shift) & clear;
    }

    /**
     * Reads an integer from the array of numbers.
     * <p>
     * Uses direct bit assembly. The array must have 4 items - because integer has 4 bytes.
     *
     * @param word     the array of 4 bytes
     * @param strategy strategy how to deal with the array. See <code>Strategy</code> class for more information.
     * @return Single integer number which combines the array of bytes into one 32-bit value
     */
    public static int readInt(Byte[] word, int strategy) {
        return readInt(word, 0, Math.min(4, word.length), strategy);
    }

    /**
     * Reads an integer from the array of numbers.
     * <p>
     * Uses direct bit assembly. The array must have exactly 4 items - because integer has 4 bytes. If the array
     * has more bytes, they are ignored. Which ones are ignored depends on the byte ordering (=strategy).
     *
     * @param word     the array of 4 bytes
     * @param strategy strategy how to deal with the array. See <code>Strategy</code> class for more information.
     * @return Single integer number which combines the array of bytes into one 32-bit value
     */
    public static int readInt(byte[] word, int strategy) {
        return readInt(word, 0, Math.min(4, word.length), strategy);
    }

    /**
     * Reads an integer from the array of numbers.
     * <p>
     * Uses direct bit assembly. The array should have up to 4 items, each one represents a byte. If the length is
     * less than 4, the array for reading is padded with zeroes from the left (in case of big endian) or from the
     * right (in case of little endian), so that the array size is 4.
     *
     * @param word        the array of 4 bytes
     * @param startOffset starting offset in the array
     * @param length      length in the array
     * @param strategy    strategy how to deal with the array. See <code>Strategy</code> class for more information.
     * @return Single integer number which combines the array of bytes into one 32-bit value
     */
    public static int readInt(byte[] word, int startOffset, int length, int strategy) {
        assert (length >= 0 && length <= 4 && word.length >= (startOffset + length) && startOffset >= 0);

        boolean littleEndian = isLittleEndian(strategy);
        boolean reverseBits = hasReverseBits(strategy);
        int value = 0;

        if (littleEndian) {
            for (int i = startOffset + length - 1; i >= startOffset; i--) {
                value = (value << 8) | readByte(word[i], reverseBits);
            }
            return value;
        }

        for (int i = startOffset, end = startOffset + length; i < end; i++) {
            value = (value << 8) | readByte(word[i], reverseBits);
        }
        return value;
    }

    /**
     * Reads an integer from the array of numbers.
     * <p>
     * Uses direct bit assembly. The array must have 4 items, each one must represent a byte. If the value in the
     * array is larger than a byte, the higher-order bits are cut.
     *
     * @param word     the array of 4 bytes
     * @param strategy strategy how to deal with the array. See <code>Strategy</code> class for more information.
     * @return Single integer number which combines the array of bytes into one 32-bit value
     */
    public static int readInt(Integer[] word, int strategy) {
        return readInt(word, 0, Math.min(4, word.length), strategy);
    }

    /**
     * Reads an integer from the array of numbers.
     * <p>
     * Uses direct bit assembly. The array must have 4 items, each one must represent a byte. If the value in the
     * array is larger than a byte, the higher-order bits are cut.
     *
     * @param word     the array of 4 bytes
     * @param strategy strategy how to deal with the array. See <code>Strategy</code> class for more information.
     * @return Single integer number which combines the array of bytes into one 32-bit value
     */
    public static int readInt(int[] word, int strategy) {
        return readInt(word, 0, Math.min(4, word.length), strategy);
    }

    /**
     * Split the value into 4 bytes.
     * <p>
     * Uses direct bit shifts.
     *
     * @param value    The value which should be split into bytes. It is assumed that it is always in native little endian.
     * @param output   The output array. Must have space for 4 bytes. If the array is larger, other elements are ignored.
     * @param strategy strategy for how to save the value. See <code>Strategy</code> class for more information.
     */
    public static void writeInt(int value, Integer[] output, int strategy) {
        int toSave = hasReverseBits(strategy) ? Integer.reverse(value) : value;
        boolean littleEndian = isLittleEndian(strategy);

        output[0] = byteAt(toSave, littleEndian, 0);
        output[1] = byteAt(toSave, littleEndian, 1);
        output[2] = byteAt(toSave, littleEndian, 2);
        output[3] = byteAt(toSave, littleEndian, 3);
    }

    /**
     * Split the value into 4 bytes.
     * <p>
     * Uses direct bit shifts.
     *
     * @param value    The value which should be split into bytes. It is assumed that it is always in native little endian.
     * @param output   The output array. Must have space for 4 bytes. If the array is larger, other elements are ignored.
     * @param strategy strategy for how to save the value. See <code>Strategy</code> class for more information.
     */
    public static void writeInt(int value, int[] output, int strategy) {
        int toSave = hasReverseBits(strategy) ? Integer.reverse(value) : value;
        boolean littleEndian = isLittleEndian(strategy);

        output[0] = byteAt(toSave, littleEndian, 0);
        output[1] = byteAt(toSave, littleEndian, 1);
        output[2] = byteAt(toSave, littleEndian, 2);
        output[3] = byteAt(toSave, littleEndian, 3);
    }

    /**
     * Split the value into 4 bytes.
     * <p>
     * Uses direct bit shifts.
     *
     * @param value    The value which should be split into bytes. It is assumed that it is always in native little endian.
     * @param output   The output array. Must have space for 4 bytes. If the array is larger, other elements are ignored.
     * @param strategy strategy for how to save the value. See <code>Strategy</code> class for more information.
     */
    public static void writeInt(int value, Byte[] output, int strategy) {
        boolean littleEndian = isLittleEndian(strategy);
        boolean reverseBits = hasReverseBits(strategy);

        output[0] = (byte) writeByte(value, littleEndian, 0, reverseBits);
        output[1] = (byte) writeByte(value, littleEndian, 1, reverseBits);
        output[2] = (byte) writeByte(value, littleEndian, 2, reverseBits);
        output[3] = (byte) writeByte(value, littleEndian, 3, reverseBits);
    }

    /**
     * Split the value into 4 bytes.
     * <p>
     * Uses direct bit shifts.
     *
     * @param value    The value which should be split into bytes. It is assumed that it is always in native little endian.
     * @param output   The output array. Must have space for 4 bytes. If the array is larger, other elements are ignored.
     * @param strategy strategy for how to save the value. See <code>Strategy</code> class for more information.
     */
    public static void writeInt(int value, byte[] output, int strategy) {
        int toSave = hasReverseBits(strategy) ? Integer.reverse(value) : value;
        boolean littleEndian = isLittleEndian(strategy);

        output[0] = (byte) byteAt(toSave, littleEndian, 0);
        output[1] = (byte) byteAt(toSave, littleEndian, 1);
        output[2] = (byte) byteAt(toSave, littleEndian, 2);
        output[3] = (byte) byteAt(toSave, littleEndian, 3);
    }

    /**
     * Split the value into 4 bytes.
     * <p>
     * Uses direct bit shifts.
     *
     * @param value    The value which should be split into bytes. It is assumed that it is always in native little endian.
     * @param output   The output array. Must have space for 4 bytes. If the array is larger, other elements are ignored.
     * @param strategy strategy for how to save the value. See <code>Strategy</code> class for more information.
     */
    public static void writeInt(int value, Short[] output, int strategy) {
        int toSave = hasReverseBits(strategy) ? Integer.reverse(value) : value;
        boolean littleEndian = isLittleEndian(strategy);

        output[0] = (short) byteAt(toSave, littleEndian, 0);
        output[1] = (short) byteAt(toSave, littleEndian, 1);
        output[2] = (short) byteAt(toSave, littleEndian, 2);
        output[3] = (short) byteAt(toSave, littleEndian, 3);
    }

    /**
     * Converts Number[] array to Byte[] array.
     * Every number is converted to byte using number.byteValue() call.
     *
     * @param numbers numbers array
     * @return boxed Byte[] array
     */
    public static Byte[] numbersToBytes(Number[] numbers) {
        Byte[] result = new Byte[numbers.length];
        for (int i = 0; i < numbers.length; i++) {
            result[i] = numbers[i].byteValue();
        }
        return result;
    }

    /**
     * Converts Number[] array to native byte[] array.
     * Every number is converted to byte using number.byteValue() call.
     *
     * @param numbers numbers array
     * @return native byte[] array
     */
    public static byte[] numbersToNativeBytes(Number[] numbers) {
        byte[] result = new byte[numbers.length];
        for (int i = 0; i < numbers.length; i++) {
            result[i] = numbers[i].byteValue();
        }
        return result;
    }

    /**
     * Converts Short[] array to Byte[] array.
     * Every number is converted to byte using number.byteValue() call.
     *
     * @param numbers numbers array
     * @return boxed Byte[] array
     */
    public static Byte[] shortsToBytes(Short[] numbers) {
        Byte[] result = new Byte[numbers.length];
        for (int i = 0; i < numbers.length; i++) {
            result[i] = numbers[i].byteValue();
        }
        return result;
    }

    /**
     * Converts Short[] array to native byte[] array.
     * Every number is converted to byte using number.byteValue() call.
     *
     * @param numbers numbers array
     * @return native byte[] array
     */
    public static byte[] shortsToNativeBytes(Short[] numbers) {
        byte[] result = new byte[numbers.length];
        for (int i = 0; i < numbers.length; i++) {
            result[i] = numbers[i].byteValue();
        }
        return result;
    }

    /**
     * Converts Short[] array to native short[] array.
     *
     * @param numbers numbers array
     * @return native short[] array
     */
    public static short[] shortsToNativeShorts(Short[] numbers) {
        short[] result = new short[numbers.length];
        for (int i = 0; i < numbers.length; i++) {
            result[i] = numbers[i];
        }
        return result;
    }

    /**
     * Converts native short[] array to native byte[] array.
     * Every number is converted to byte using number.byteValue() call.
     *
     * @param numbers numbers array
     * @return native byte[] array
     */
    public static byte[] nativeShortsToNativeBytes(short[] numbers) {
        byte[] result = new byte[numbers.length];
        for (int i = 0; i < numbers.length; i++) {
            result[i] = (byte) (numbers[i] & 0xFF);
        }
        return result;
    }

    /**
     * Converts native short[] array to native byte[] array.
     * Every number is converted to byte using number.byteValue() call.
     *
     * @param numbers numbers array
     * @return native byte[] array
     */
    public static byte[] nativeIntsToNativeBytes(int[] numbers) {
        byte[] result = new byte[numbers.length];
        for (int i = 0; i < numbers.length; i++) {
            result[i] = (byte) (numbers[i] & 0xFF);
        }
        return result;
    }

    /**
     * Converts native short[] array to Byte[] array.
     * Every number is converted to byte using number &amp; 0xFF
     *
     * @param numbers numbers array
     * @return boxed Byte[] array
     */
    public static Byte[] nativeShortsToBytes(short[] numbers) {
        Byte[] result = new Byte[numbers.length];
        for (int i = 0; i < numbers.length; i++) {
            result[i] = (byte) (numbers[i] & 0xFF);
        }
        return result;
    }

    /**
     * Converts native short[] array to Short[] array.
     * Every number is converted to byte using number.byteValue() call.
     *
     * @param numbers numbers array
     * @return boxed Short[] array
     */
    public static Short[] nativeShortsToShorts(short[] numbers) {
        Short[] result = new Short[numbers.length];
        for (int i = 0; i < numbers.length; i++) {
            result[i] = numbers[i];
        }
        return result;
    }

    /**
     * Converts native byte[] array to boxed Byte[] array.
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
     * Converts native byte[] array to boxed Integer[] array.
     *
     * @param array native byte[] array
     * @return boxed Integer[] array
     */
    public static Integer[] nativeBytesToIntegers(byte[] array) {
        Integer[] result = new Integer[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i] & 0xFF;
        }
        return result;
    }

    /**
     * Converts native byte[] array to boxed Short[] array.
     *
     * @param array native byte[] array
     * @return boxed Short[] array
     */
    public static Short[] nativeBytesToShorts(byte[] array) {
        Short[] result = new Short[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = (short) (array[i] & 0xFF);
        }
        return result;
    }

    /**
     * Converts native byte[] array to boxed Integer[] array.
     *
     * @param array native byte[] array
     * @return boxed Integer[] array
     */
    public static int[] nativeBytesToInts(byte[] array) {
        int[] result = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[i] & 0xFF;
        }
        return result;
    }

    /**
     * Converts list of Integers into array of native ints.
     *
     * @param list list of integers
     * @return native int[] array
     */
    public static int[] listToNativeInts(List<Integer> list) {
        int[] result = new int[list.size()];
        if (list instanceof RandomAccess) {
            for (int i = 0; i < result.length; i++) {
                result[i] = list.get(i);
            }
            return result;
        }

        int index = 0;
        for (Integer value : list) {
            result[index++] = value;
        }
        return result;
    }

    private static int readInt(Integer[] word, int startOffset, int length, int strategy) {
        boolean littleEndian = isLittleEndian(strategy);
        boolean reverseBits = hasReverseBits(strategy);
        int value = 0;

        if (littleEndian) {
            for (int i = startOffset + length - 1; i >= startOffset; i--) {
                value = (value << 8) | readByte(word[i], reverseBits);
            }
            return value;
        }

        for (int i = startOffset, end = startOffset + length; i < end; i++) {
            value = (value << 8) | readByte(word[i], reverseBits);
        }
        return value;
    }

    private static int readInt(Byte[] word, int startOffset, int length, int strategy) {
        boolean littleEndian = isLittleEndian(strategy);
        boolean reverseBits = hasReverseBits(strategy);
        int value = 0;

        if (littleEndian) {
            for (int i = startOffset + length - 1; i >= startOffset; i--) {
                value = (value << 8) | readByte(word[i], reverseBits);
            }
            return value;
        }

        for (int i = startOffset, end = startOffset + length; i < end; i++) {
            value = (value << 8) | readByte(word[i], reverseBits);
        }
        return value;
    }

    private static int readInt(int[] word, int startOffset, int length, int strategy) {
        boolean littleEndian = isLittleEndian(strategy);
        boolean reverseBits = hasReverseBits(strategy);
        int value = 0;

        if (littleEndian) {
            for (int i = startOffset + length - 1; i >= startOffset; i--) {
                value = (value << 8) | readByte(word[i], reverseBits);
            }
            return value;
        }

        for (int i = startOffset, end = startOffset + length; i < end; i++) {
            value = (value << 8) | readByte(word[i], reverseBits);
        }
        return value;
    }

    private static boolean isLittleEndian(int strategy) {
        return (strategy & Strategy.BIG_ENDIAN) != Strategy.BIG_ENDIAN;
    }

    private static boolean hasReverseBits(int strategy) {
        return (strategy & Strategy.REVERSE_BITS) == Strategy.REVERSE_BITS;
    }

    private static int readByte(byte value, boolean reverseBits) {
        return reverseBits ? reverseByte(value) : (value & 0xFF);
    }

    private static int readByte(Number value, boolean reverseBits) {
        int byteValue = value.byteValue();
        return reverseBits ? reverseByte(byteValue) : (byteValue & 0xFF);
    }

    private static int writeByte(int value, boolean littleEndian, int index, boolean reverseBits) {
        int byteValue = byteAt(value, littleEndian, index);
        return reverseBits ? reverseByte(byteValue) : byteValue;
    }

    private static int byteAt(int value, boolean littleEndian, int index) {
        int shift = littleEndian ? (index << 3) : (24 - (index << 3));
        return (value >>> shift) & 0xFF;
    }

    private static int reverseByte(int value) {
        return REVERSED_BYTES[value & 0xFF] & 0xFF;
    }

    private static byte[] createReversedBytes() {
        byte[] reversedBytes = new byte[256];
        for (int i = 0; i < reversedBytes.length; i++) {
            reversedBytes[i] = (byte) (Integer.reverse(i) >>> 24);
        }
        return reversedBytes;
    }

    /**
     * Converts packed BCD code (1 byte, 2 BCD digits) to binary
     * It is assumed the BCD has little endian.
     *
     * @param bcd packed BCD code
     * @return binary number
     */
    public static int bcd2bin(int bcd) {
        return ((bcd >> 4) & 0xF) * 10 + (bcd & 0xF);
    }

    /**
     * Converts a binary number into packed BCD (1 byte, 2 BCD digits)
     *
     * @param bin binary number
     * @return number in packed BCD code, little endian
     */
    public static int bin2bcd(int bin) {
        return ((((bin / 10) & 0xF) << 4) + ((bin % 10) & 0xF));
    }
}
