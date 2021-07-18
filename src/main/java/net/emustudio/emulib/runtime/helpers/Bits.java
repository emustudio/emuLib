package net.emustudio.emulib.runtime.helpers;

import java.nio.ByteBuffer;

public class Bits {
    public int bits;
    public final int length;
    private final int mask;

    public Bits(int bits, int length) {
        this.bits = bits;
        this.length = length;
        this.mask = (0xFFFFFFFF >>> (32 - length));
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
     *
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
     *
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
