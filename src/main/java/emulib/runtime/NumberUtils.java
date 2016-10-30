/*
 * KISS, YAGNI, DRY
 *
 * (c) Copyright 2006-2016, Peter Jakubčo
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package emulib.runtime;

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
     * Reads an integer from the array of numbers.
     * 
     * Uses binary arithmetic. The array must have 4 items, each one must represent a byte. If the value in the
     * array is larger than a byte, the higher-order bits are cut.
     * 
     * @param <T> type of the array. Must be a Number
     * @param word the array of bytes
     * @param strategy strategy how to deal with the array. See <code>Strategy</code> class for more information.
     * @return Single integer number which combines the array of bytes into one 32-bit value
     */
    public static <T extends Number> int readInt(T[] word, int strategy) {
        int result;
  
        if ((strategy & Strategy.BIG_ENDIAN) == Strategy.BIG_ENDIAN) {
            result = (word[3].intValue() & 0xFF)
                | ((word[2].intValue() & 0xFF) << 8)
                | ((word[1].intValue() & 0xFF) << 16)
                | ((word[0].intValue() & 0xFF) << 24);
        } else {
            result = (word[0].intValue() & 0xFF)
                | ((word[1].intValue() & 0xFF) << 8)
                | ((word[2].intValue() & 0xFF) << 16)
                | ((word[3].intValue() & 0xFF) << 24);
        }
        
        if ((strategy & Strategy.REVERSE_BITS) == Strategy.REVERSE_BITS) {
            result = reverseBits(result, 32);
        }
        return result;
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
    public static void writeInt(int value, Integer[] output, int strategy) {
        if ((strategy & Strategy.BIG_ENDIAN) == Strategy.BIG_ENDIAN) {
            output[3] = value & 0xFF;
            output[2] = (value >>> 8) & 0xFF;
            output[1] = (value >>> 16) & 0xFF;
            output[0] = (value >>> 24) & 0xFF;
        } else {
            output[0] = value & 0xFF;
            output[1] = (value >>> 8) & 0xFF;
            output[2] = (value >>> 16) & 0xFF;
            output[3] = (value >>> 24) & 0xFF;
        }
        
        if ((strategy & Strategy.REVERSE_BITS) == Strategy.REVERSE_BITS) {
            output[0] = reverseBits(output[0], 8);
            output[1] = reverseBits(output[1], 8);
            output[2] = reverseBits(output[2], 8);
            output[3] = reverseBits(output[3], 8);
        }
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
    public static void writeInt(int value, Byte[] output, int strategy) {
        Integer[] tmp = new Integer[4];
        writeInt(value, tmp, strategy);
        
        for (int i = 0; i < tmp.length; i++) {
            output[i] = (byte)(tmp[i] & 0xFF);
        }
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
        Integer[] tmp = new Integer[output.length];
        writeInt(value, tmp, strategy);
        
        for (int i = 0; i < tmp.length; i++) {
            output[i] = (short)(tmp[i] & 0xFFFF);
        }
    }

}
