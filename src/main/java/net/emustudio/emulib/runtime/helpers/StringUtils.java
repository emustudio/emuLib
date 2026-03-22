/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.helpers;

import java.awt.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * String utility class.
 */
public class StringUtils {

    /**
     * Constructs a new StringUtils instance.
     */
    private StringUtils() {
    }

    /**
     * Shortens a string to a given length.
     * It does it by selecting some string prefix and suffix, and inserts "..." in the middle.
     *
     * @param string    original string
     * @param maxLength max string length (number of chars)
     * @return shortened string
     */
    public static String shorten(String string, int maxLength) {
        int length = string.length();
        if (length <= maxLength) {
            return string;
        }

        int maxLengthNoDots = Math.max(maxLength - 3, 0);
        int charsShown = maxLengthNoDots / 2;
        int carry = maxLengthNoDots % 2;

        // shorten string with 3 dots if it is too long
        return string.substring(0, charsShown) + "..." + string.substring(length - charsShown - carry);
    }

    /**
     * Shortens a string to a given length.
     * Will find out the max length of the string by using the component width and component font.
     *
     * @param string         original string
     * @param component      component for getting font size (string width)
     * @param componentWidth component width
     * @return shortened string
     */
    public static String shorten(String string, Component component, int componentWidth) {
        FontMetrics fontMetrics = component.getFontMetrics(component.getFont());
        int baseNameWidth = fontMetrics.stringWidth(string);
        int maxPathLength = string.length() * Math.min(componentWidth, baseNameWidth) / baseNameWidth;
        return shorten(string, maxPathLength);
    }

    /**
     * Transforms bits into a meaningful string using the formatting character.
     *
     * @param format the formatting character ('s' for a string, etc.)
     * @param bits   the bits to format
     * @return the resulting string
     */
    public static String format(char format, Bits bits) {
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
                        // Bits only stores 32-bit int, but we need 8 bytes for double.
                        ByteBuffer buffer = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
                        buffer.put(bits.toBytes());
                        buffer.position(0);
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
