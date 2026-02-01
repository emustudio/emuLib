/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.ui;

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
                return (!string.isEmpty()) ? string.substring(0, 1) : "?";
            case 'd':
                return Integer.toString(bits.number);
            case 'f':
                switch (bits.length) {
                    case 32:
                        return Float.toString(ByteBuffer.wrap(bits.toBytes()).order(ByteOrder.LITTLE_ENDIAN).getFloat());
                    case 64:
                        // Bits only stores 32-bit int, but we need 8 bytes for double
                        // Extend the 4-byte array to 8 bytes for proper double interpretation
                        ByteBuffer buffer = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
                        buffer.put(bits.toBytes());
                        buffer.position(0); // Reset position to read from start
                        return Double.toString(buffer.getDouble());
                    default:
                        return "NaN";
                }
            case 's':
                return new String(bits.toBytes()).replace("\0", "");
            case 'x':
                return Integer.toHexString(bits.number);
            case 'X':
                return Integer.toHexString(bits.number).toUpperCase();
            case '%':
                return "%";
            default:
                return Character.toString(format);
        }
    }
}
