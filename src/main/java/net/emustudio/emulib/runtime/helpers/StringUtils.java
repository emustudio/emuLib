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
