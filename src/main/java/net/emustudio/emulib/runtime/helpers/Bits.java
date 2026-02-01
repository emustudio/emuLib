/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.helpers;

import net.jcip.annotations.NotThreadSafe;

import java.nio.ByteBuffer;

/**
 * Bits utility class. Supports various operations on bits.
 * It supports maximally 32 bits, because it works with an integer.
 */
@NotThreadSafe
public class Bits {
    public int number;
    public final int length;
    private final int mask;

    /**
     * Constructs new Bits object.
     *
     * @param number integer number in little endian
     * @param length significant bits count
     */
    public Bits(int number, int length) {
        this.number = number;
        this.length = length;
        this.mask = (1 << length) - 1;
    }

    /**
     * Converts the bits to byte array in little endian. The size of the array is always 4 bytes.
     *
     * @return byte array representing the bits in little endian
     */
    @SuppressWarnings("unused")
    public byte[] toBytes() {
        return ByteBuffer.allocate(4).putInt(number).array();
    }

    /**
     * Reverses the bytes.
     *
     * @return this Bits, with bytes reversed
     */
    public Bits reverseBytes() {
        // if length = 8
        //
        // little endian
        // [ FF 00 00 00 ] = 0xFF   => [ 00 00 00 FF ] = 0xFF000000 >> (32 - 8 = 24) = 0xFF
        number = Integer.reverseBytes(number) >>> (32 - length);
        return this;
    }

    /**
     * Reverses bits in each byte.
     * <p>
     * However, order of bytes is kept.
     *
     * @return this Bits, reversed bits within each byte
     */
    public Bits reverseBits() {
        number = NumberUtils.reverseBits(number, length);
        return this;
    }

    /**
     * Make the absolute value from the number stored in two's complement.
     * <p>
     * If the number is already positive, it is kept as-is.
     *
     * @return this Bits - the negative sign is removed
     */
    public Bits absolute() {
        number = Math.abs(number);
        return this;
    }

    /**
     * Shift the value to the left.
     *
     * @return this Bits with value shifted to the left by 1 bit, padded with zeroes from the right
     */
    public Bits shiftLeft() {
        number = (number << 1) & mask;
        return this;
    }

    /**
     * Shift the value to the right.
     *
     * @return this Bits shifted to the right by 1 bit, padded with zeroes from the left
     */
    public Bits shiftRight() {
        number >>>= 1;
        return this;
    }
}
