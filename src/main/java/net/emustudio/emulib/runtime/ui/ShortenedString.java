/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.ui;

import net.jcip.annotations.NotThreadSafe;

import java.awt.*;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A shortened string from a value. A string computed from given value can be shortened to fit the component width.
 *
 * @param <T> type of the value
 */
@NotThreadSafe
public class ShortenedString<T> {
    private static final Pattern CHARACTER = Pattern.compile("\\X");
    private final T value;
    private final Function<T, String> toString;
    private int maxStringLength;

    /**
     * Creates a shortened string from a value.
     *
     * @param value    value
     * @param toString function to convert the value to a string
     */
    public ShortenedString(T value, Function<T, String> toString) {
        this.value = Objects.requireNonNull(value);
        this.toString = Objects.requireNonNull(toString);
    }

    /**
     * Derives the maximum length of the shortened string to fit the component width.
     * <p>
     * Should be called when the component width, font, or displayed value changes.
     * Non-positive widths produce an empty string.
     *
     * @param component      component
     * @param componentWidth custom component width (if it's different to Component.getWidth())
     */
    public void deriveMaxStringLength(Component component, int componentWidth) {
        if (componentWidth <= 0) {
            maxStringLength = 0;
            return;
        }
        FontMetrics fontMetrics = component.getFontMetrics(component.getFont());
        String fullString = getFullString();
        int fullStringWidth = fontMetrics.stringWidth(fullString);

        if (fullStringWidth <= componentWidth) {
            this.maxStringLength = fullString.length();
            return;
        }
        int low = 0;
        int high = fullString.length() - 1;
        maxStringLength = 0;
        while (low <= high) {
            int candidate = low + (high - low) / 2;
            if (fontMetrics.stringWidth(shorten(fullString, candidate)) <= componentWidth) {
                maxStringLength = candidate;
                low = candidate + 1;
            } else {
                high = candidate - 1;
            }
        }
    }

    /**
     * Derives the maximum length of the shortened string to fit the component width.
     * <p>
     * Should be called when the component width changes.
     *
     * @param component component
     */
    public void deriveMaxStringLength(Component component) {
        deriveMaxStringLength(component, component.getWidth());
    }

    /**
     * Get the derived maximum length of the shortened string.
     *
     * @return max string length
     */
    public int getMaxStringLength() {
        return maxStringLength;
    }

    /**
     * Get the original value
     *
     * @return value
     */
    public T getValue() {
        return value;
    }

    /**
     * Get the shortened String.
     * <p>
     * A shortened string is cut off at derived max string length. It does it by selecting some string prefix and
     * suffix, and inserts "..." in the middle. Unicode character sequences are kept intact.
     * If there is insufficient room for three dots, only the dots that fit are shown.
     *
     * @return shortened string
     */
    public String getShortenedString() {
        return shorten(toString.apply(value), maxStringLength);
    }

    /**
     * Shortens a string to a given length by keeping a prefix and suffix and inserting "..." in the middle.
     *
     * @param string    original string
     * @param maxLength max string length (number of chars)
     * @return shortened string
     */
    private static String shorten(String string, int maxLength) {
        int length = string.length();
        if (length <= maxLength) {
            return string;
        }
        if (maxLength < 3) {
            return "...".substring(0, maxLength);
        }

        int visible = maxLength - 3;
        int prefixBudget = visible / 2;
        int suffixBoundary = length - (visible - prefixBudget);
        int prefixEnd = 0;
        int suffixStart = length;
        Matcher characters = CHARACTER.matcher(string);
        while (characters.find()) {
            if (characters.end() <= prefixBudget) {
                prefixEnd = characters.end();
            }
            if (characters.start() >= suffixBoundary) {
                suffixStart = characters.start();
                break;
            }
        }
        return string.substring(0, prefixEnd) + "..." + string.substring(suffixStart);
    }

    /**
     * Get the full string.
     *
     * @return full string
     */
    public String getFullString() {
        return toString.apply(value);
    }

    @Override
    public String toString() {
        return getShortenedString();
    }
}
