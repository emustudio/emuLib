package net.emustudio.emulib.plugins.cpu;

/**
 * Constant-decoding strategies.
 *
 * It is used in Edigen specification files for decoding numbers then used in disassembler.
 */
public class DecodingStrategy {

    /**
     * Reverses the bytes.
     *
     * @param value array of bytes
     * @return reversed array of bytes
     */
    public static byte[] reverseBytes(byte[] value) {
        byte[] result = new byte[value.length];

        for (int i = 0; i < result.length; i++) {
            result[i] = value[value.length - i - 1];
        }

        return result;
    }

    /**
     * Reverses bits in each byte.
     *
     * However, order of bytes is kept.
     *
     * @param value array of bytes
     * @return array of bytes with bits reversed
     */
    public static byte[] reverseBits(byte[] value) {
        byte[] result = new byte[value.length];

        for (int octet = 0; octet < result.length; octet++) {
            for (int bit = 0; bit < 8; bit++) {
                result[octet] |= (value[octet] & (1 << bit)) >>> bit << (8 - bit - 1);
            }
        }

        return result;
    }

    /**
     * Make the absolute value from the number stored in two's complement.
     *
     * If the number is already positive, it is kept as-is.
     *
     * @param value array of bytes representing the two's complement number
     * @return absolute value (two's complement is removed) - the negative sign is lost
     */
    public static byte[] absolute(byte[] value) {
        byte[] result = new byte[value.length];
        if (value.length > 0) {
            if ((value[0] & 0b10000000) != 0) {
                for (int octet = 0; octet < result.length; octet++) {
                    result[octet] = (byte)(~value[octet] & 0xFF);
                }
                for (int octet = 0; octet < result.length; octet++) {
                    if ((short)result[octet] + 1 > (byte)(result[octet] + 1)) {
                        result[octet] = 0;
                    } else {
                        result[octet] = (byte)(result[octet] + 1);
                        break;
                    }
                }
            } else {
                result = value;
            }
        }
        return result;
    }

    /**
     * Shift the byte array to the left.
     *
     * It is assumed that the bytes are stored as big-endian. The shifting operation does as follows:
     * If we have bytes {@code [0x1, 0x2]} (memory positions {@code [0]=0x1}, {@code [1]=0x2}), then the result will be
     * {@code [0x2, 0x4]}.
     *
     * @param value array of bytes
     * @return byte array shifted by 1 bit to the left
     */
    public static byte[] shiftLeft(byte[] value) {
        byte[] result = new byte[value.length];

        for (int octet = 0; octet < result.length; octet++) {
            int shifted = (value[octet] << 1) & 0xFF;
            if (octet + 1 < result.length) {
                shifted |= value[octet + 1] >>> 7;
            }
            result[octet] = (byte)shifted;
        }
        return result;
    }

    /**
     * Shift the byte array to the right.
     *
     * It is assumed that the bytes are stored as big-endian. The shifting operation does as follows:
     * If we have bytes {@code [0x1, 0x2]} (memory positions {@code [0]=0x1}, {@code [1]=0x2}), then the result will be
     * {@code [0x0, 0x81]}.
     *
     * @param value array of bytes
     * @return byte array shifted by 1 bit to the right
     */
    public static byte[] shiftRight(byte[] value) {
        byte[] result = new byte[value.length];

        for (int octet = result.length - 1; octet >= 0; octet--) {
            int shifted = (value[octet] >>> 1) & 0xFF;
            if (octet - 1 >= 0) {
                shifted |= ((value[octet - 1] & 1) << 7);
            }
            result[octet] = (byte)shifted;
        }
        return result;
    }
}
