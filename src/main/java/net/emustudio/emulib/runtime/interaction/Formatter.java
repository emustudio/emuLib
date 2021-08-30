package net.emustudio.emulib.runtime.interaction;

import net.emustudio.emulib.runtime.helpers.Bits;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@SuppressWarnings("unused")
public interface Formatter {
    Formatter DEFAULT = new Formatter() {
    };

    /**
     * Transforms the bytes into a meaningful string using the formatting
     * character.
     *
     * @param format the formatting character ('s' for a string, etc.)
     * @param bits   the array of bytes
     * @return the resulting string
     */
    default String format(char format, Bits bits) {
        switch (format) {
            case 'c':
                String string = new String(bits.toBytes());
                return (string.length() != 0) ? string.substring(0, 1) : "?";
            case 'd':
                return Integer.toString(bits.bits);
            case 'f':
                switch (bits.length) {
                    case 32:
                        return Float.toString(ByteBuffer.wrap(bits.toBytes()).order(ByteOrder.LITTLE_ENDIAN).getFloat());
                    case 64:
                        return Double.toString(ByteBuffer.wrap(bits.toBytes()).order(ByteOrder.LITTLE_ENDIAN).getDouble());
                    default:
                        return "NaN";
                }
            case 's':
                return new String(bits.toBytes()).replace("\0", "");
            case 'x':
                return Integer.toHexString(bits.bits);
            case 'X':
                return Integer.toHexString(bits.bits).toUpperCase();
            case '%':
                return "%";
            default:
                return Character.toString(format);
        }
    }
}
