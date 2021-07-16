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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class NumberUtils {
    
    /**
     * Strategy defining how to manipulate with bytes. 
     * 
     * Strategies can be combined with | (or) operator.
     */
    public static final class Strategy {
        
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
     * @param value value which bits will be reversed
     * @param numberOfBits how many bits should be reversed. If the value has more bits, the rest will be ignored.
     * @return value with reversed bits
     */
    public static int reverseBits(int value, int numberOfBits) {
        int result = 0;
        for (int i = 0; i < numberOfBits; i++) {
            result |= ((value >>> i) & 0x1) << (numberOfBits - i - 1);
        }
        return result;
    }

    /**
     * Reverse bits in long (max 64-bit) value.
     *
     * @param value value which bits will be reversed
     * @param numberOfBits how many bits should be reversed. If the value has more bits, the rest will be ignored.
     * @return value with reversed bits
     */
    public static long reverseBits(long value, int numberOfBits) {
        long result = 0;
        for (int i = 0; i < numberOfBits; i++) {
            result |= ((value >>> i) & 0x1) << (numberOfBits - i - 1);
        }
        return result;
    }

    /**
     * Reads an integer from the array of numbers.
     * 
     * Uses ByteBuffer.wrap. The array must have 4 items, each one must represent a byte. If the value in the
     * array is larger than a byte, the higher-order bits are cut.
     * 
     * @param word the array of 4 bytes
     * @param strategy strategy how to deal with the array. See <code>Strategy</code> class for more information.
     * @return Single integer number which combines the array of bytes into one 32-bit value
     */
    public static int readInt(Byte[] word, int strategy) {
        return readInt(numbersToNativeBytes(word), strategy);
    }

    /**
     * Reads an integer from the array of numbers.
     *
     * Uses ByteBuffer.wrap. The array must have 4 items, each one must represent a byte. If the value in the
     * array is larger than a byte, the higher-order bits are cut.
     *
     * @param word the array of 4 bytes
     * @param strategy strategy how to deal with the array. See <code>Strategy</code> class for more information.
     * @return Single integer number which combines the array of bytes into one 32-bit value
     */
    public static int readInt(byte[] word, int strategy) {
        ByteBuffer wrapped = ByteBuffer.wrap(word,0, 4);

        if ((strategy & Strategy.BIG_ENDIAN) != Strategy.BIG_ENDIAN) {
            wrapped.order(ByteOrder.LITTLE_ENDIAN);
        }
        int result = wrapped.getInt();

        if ((strategy & Strategy.REVERSE_BITS) == Strategy.REVERSE_BITS) {
            return reverseBits(result, 32);
        }
        return result;
    }
    
    /**
     * Reads an integer from the array of numbers.
     * 
     * Uses ByteBuffer.wrap. The array must have 4 items, each one must represent a byte. If the value in the
     * array is larger than a byte, the higher-order bits are cut.
     * 
     * @param word the array of 4 bytes
     * @param strategy strategy how to deal with the array. See <code>Strategy</code> class for more information.
     * @return Single integer number which combines the array of bytes into one 32-bit value
     */
    public static int readInt(Integer[] word, int strategy) {
        return readInt(numbersToNativeBytes(word), strategy);
    }

    /**
     * Reads an integer from the array of numbers.
     *
     * Uses ByteBuffer.wrap. The array must have 4 items, each one must represent a byte. If the value in the
     * array is larger than a byte, the higher-order bits are cut.
     *
     * @param word the array of 4 bytes
     * @param strategy strategy how to deal with the array. See <code>Strategy</code> class for more information.
     * @return Single integer number which combines the array of bytes into one 32-bit value
     */
    public static int readInt(int[] word, int strategy) {
        return readInt(nativeIntsToNativeBytes(word), strategy);
    }
    
    /**
     * Split the value into 4 bytes.
     * 
     * Uses ByteBuffer.
     * 
     * @param value The value which should be split into bytes. It is assumed that it is always in native little endian.
     * @param output The output array. Must have space for 4 bytes. If the array is larger, other elements are ignored.
     * @param strategy strategy for how to save the value. See <code>Strategy</code> class for more information.
     */
    public static void writeInt(int value, Integer[] output, int strategy) {
        int toSave = value;
        if ((strategy & Strategy.REVERSE_BITS) == Strategy.REVERSE_BITS) {
            toSave = reverseBits(value, 32);
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        if ((strategy & Strategy.BIG_ENDIAN) != Strategy.BIG_ENDIAN) {
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        }
        byteBuffer.putInt(toSave);
        System.arraycopy(nativeBytesToIntegers(byteBuffer.array()), 0, output, 0, 4);
    }

    /**
     * Split the value into 4 bytes.
     *
     * Uses ByteBuffer.
     *
     * @param value The value which should be split into bytes. It is assumed that it is always in native little endian.
     * @param output The output array. Must have space for 4 bytes. If the array is larger, other elements are ignored.
     * @param strategy strategy for how to save the value. See <code>Strategy</code> class for more information.
     */
    public static void writeInt(int value, int[] output, int strategy) {
        int toSave = value;
        if ((strategy & Strategy.REVERSE_BITS) == Strategy.REVERSE_BITS) {
            toSave = reverseBits(value, 32);
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        if ((strategy & Strategy.BIG_ENDIAN) != Strategy.BIG_ENDIAN) {
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        }
        byteBuffer.putInt(toSave);
        System.arraycopy(nativeBytesToInts(byteBuffer.array()), 0, output, 0, 4);
    }

    /**
     * Split the value into 4 bytes.
     * 
     * Uses ByteBuffer.
     * 
     * @param value The value which should be split into bytes. It is assumed that it is always in native little endian.
     * @param output The output array. Must have space for 4 bytes. If the array is larger, other elements are ignored.
     * @param strategy strategy for how to save the value. See <code>Strategy</code> class for more information.
     */
    public static void writeInt(int value, Byte[] output, int strategy) {
        int toSave = value;
        if ((strategy & Strategy.REVERSE_BITS) == Strategy.REVERSE_BITS) {
            toSave = reverseBits(value, 32);
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        if ((strategy & Strategy.BIG_ENDIAN) != Strategy.BIG_ENDIAN) {
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        }
        byteBuffer.putInt(toSave);
        System.arraycopy(nativeBytesToBytes(byteBuffer.array()), 0, output, 0, 4);
    }

    /**
     * Split the value into 4 bytes.
     *
     * Uses ByteBuffer.
     *
     * @param value The value which should be split into bytes. It is assumed that it is always in native little endian.
     * @param output The output array. Must have space for 4 bytes. If the array is larger, other elements are ignored.
     * @param strategy strategy for how to save the value. See <code>Strategy</code> class for more information.
     */
    public static void writeInt(int value, byte[] output, int strategy) {
        int toSave = value;
        if ((strategy & Strategy.REVERSE_BITS) == Strategy.REVERSE_BITS) {
            toSave = reverseBits(value, 32);
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        if ((strategy & Strategy.BIG_ENDIAN) != Strategy.BIG_ENDIAN) {
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        }
        byteBuffer.putInt(toSave);
        System.arraycopy(byteBuffer.array(), 0, output, 0, 4);
    }

    /**
     * Split the value into 4 bytes.
     * 
     * Uses binary arithmetic. 
     * 
     * @param value The value which should be split into bytes. It is assumed that it is always in native little endian.
     * @param output The output array. Must have space for 4 bytes. If the array is larger, other elements are ignored.
     * @param strategy strategy for how to save the value. See <code>Strategy</code> class for more information.
     */
    public static void writeInt(int value, Short[] output, int strategy) {
        int toSave = value;
        if ((strategy & Strategy.REVERSE_BITS) == Strategy.REVERSE_BITS) {
            toSave = reverseBits(value, 32);
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        if ((strategy & Strategy.BIG_ENDIAN) != Strategy.BIG_ENDIAN) {
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        }
        byteBuffer.putInt(toSave);
        System.arraycopy(nativeBytesToShorts(byteBuffer.array()), 0, output, 0, 4);
    }

    /**
     * Converts Number[] array to Byte[] array.
     * Every number is converted to byte using number.byteValue() call.
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
     * @param numbers numbers array
     * @return native byte[] array
     */
    public static byte[] nativeShortsToNativeBytes(short[] numbers) {
        byte[] result = new byte[numbers.length];
        for (int i = 0; i < numbers.length; i++) {
            result[i] = (byte)(numbers[i] & 0xFF);
        }
        return result;
    }

    /**
     * Converts native short[] array to native byte[] array.
     * Every number is converted to byte using number.byteValue() call.
     * @param numbers numbers array
     * @return native byte[] array
     */
    public static byte[] nativeIntsToNativeBytes(int[] numbers) {
        byte[] result = new byte[numbers.length];
        for (int i = 0; i < numbers.length; i++) {
            result[i] = (byte)(numbers[i] & 0xFF);
        }
        return result;
    }

    /**
     * Converts native short[] array to Byte[] array.
     * Every number is converted to byte using number &amp; 0xFF
     * @param numbers numbers array
     * @return boxed Byte[] array
     */
    public static Byte[] nativeShortsToBytes(short[] numbers) {
        Byte[] result = new Byte[numbers.length];
        for (int i = 0; i < numbers.length; i++) {
            result[i] = (byte)(numbers[i] & 0xFF);
        }
        return result;
    }

    /**
     * Converts native short[] array to Short[] array.
     * Every number is converted to byte using number.byteValue() call.
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
}
