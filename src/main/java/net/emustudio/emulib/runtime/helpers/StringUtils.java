/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.helpers;

import java.awt.*;

public class StringUtils {

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
}
