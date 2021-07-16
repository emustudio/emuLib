package net.emustudio.emulib.plugins.cpu;

import net.emustudio.emulib.runtime.helpers.NumberUtils;

/**
 * Constant-decoding strategies.
 *
 * It is used in Edigen specification files for decoding numbers then used in disassembler.
 */
public class DecodingStrategy {

    /**
     * Reverses the bytes.
     *
     * @param value value
     * @return value with reversed bytes
     */
    public static int reverseBytes(int value) {
        return Integer.reverseBytes(value);
    }

    /**
     * Reverses bits in each byte.
     *
     * However, order of bytes is kept.
     *
     * @param value the value
     * @return absolute value, reversed bits within each byte
     */
    public static int reverseBits(int value) {
        return Integer.reverseBytes(NumberUtils.reverseBits(value, 32));
    }

    /**
     * Make the absolute value from the number stored in two's complement.
     *
     * If the number is already positive, it is kept as-is.
     *
     * @param value two's complement number
     * @return absolute value (two's complement is removed) - the negative sign is lost
     */
    public static int absolute(int value) {
        return Math.abs(value);
    }

    /**
     * Shift the value to the left.
     *
     * @param value value
     * @return value shifted to the left by 1 bit, padded with zeroes from the right
     */
    public static int shiftLeft(int value) {
        return value << 1;
    }

    /**
     * Shift the value to the right.
     *
     * @param value value
     * @return value shifted to the right by 1 bit, padded with zeroes from the left
     */
    public static int shiftRight(int value) {
        return value >>> 1;
    }
}
