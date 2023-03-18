/*
 * This file is part of emuLib.
 *
 * Copyright (C) 2006-2023  Peter Jakubčo
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

import net.jcip.annotations.NotThreadSafe;

import java.nio.ByteBuffer;

/**
 * Bits utility class. Supports various operations on bits.
 */
@NotThreadSafe
public class Bits {
    public int bits;
    public final int length;
    private final int mask;

    /**
     * Constructs new Bits object.
     *
     * @param bits   integer number in little endian
     * @param length significant bits count
     */
    public Bits(int bits, int length) {
        this.bits = bits;
        this.length = length;
        this.mask = (1 << length) - 1;
    }

    @SuppressWarnings("unused")
    public byte[] toBytes() {
        return ByteBuffer.allocate(4).putInt(bits).array();
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
        bits = Integer.reverseBytes(bits) >>> (32 - length);
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
        bits = NumberUtils.reverseBits(bits, length);
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
        bits = Math.abs(bits);
        return this;
    }

    /**
     * Shift the value to the left.
     *
     * @return this Bits with value shifted to the left by 1 bit, padded with zeroes from the right
     */
    public Bits shiftLeft() {
        bits = (bits << 1) & mask;
        return this;
    }

    /**
     * Shift the value to the right.
     *
     * @return this Bits shifted to the right by 1 bit, padded with zeroes from the left
     */
    public Bits shiftRight() {
        bits >>>= 1;
        return this;
    }
}
