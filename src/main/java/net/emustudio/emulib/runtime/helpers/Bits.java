/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.helpers;

import net.jcip.annotations.Immutable;

import java.nio.ByteBuffer;

/**
 * Bits utility class. Supports various operations on bits.
 * It supports maximally 32 bits, because it works with an integer.
 */
@Immutable
public class Bits {
    /**
     * The integer number in little endian.
     */
    public final int number;
    /**
     * Significant bits count.
     */
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
     * @return a new {@link Bits} instance with bytes reversed
     */
    public Bits reverseBytes() {
        // if length = 8
        //
        // little endian
        // [ FF 00 00 00 ] = 0xFF   => [ 00 00 00 FF ] = 0xFF000000 >> (32 - 8 = 24) = 0xFF
        return new Bits(Integer.reverseBytes(number) >>> (32 - length), length);
    }

    /**
     * Reverses bits in each byte.
     * <p>
     * However, order of bytes is kept.
     *
     * @return a new {@link Bits} instance with reversed bits within each byte
     */
    public Bits reverseBits() {
        return new Bits(NumberUtils.reverseBits(number, length), length);
    }

    /**
     * Make the absolute value from the number stored in two's complement.
     * <p>
     * If the number is already positive, it is kept as-is.
     *
     * @return a new {@link Bits} instance with the negative sign removed
     */
    public Bits absolute() {
        return new Bits(Math.abs(number), length);
    }

    /**
     * Shift the value to the left.
     *
     * @return a new {@link Bits} instance shifted to the left by 1 bit
     */
    public Bits shiftLeft() {
        return new Bits((number << 1) & mask, length);
    }

    /**
     * Shift the value to the right.
     *
     * @return a new {@link Bits} instance shifted to the right by 1 bit
     */
    public Bits shiftRight() {
        return new Bits(number >>> 1, length);
    }
}
