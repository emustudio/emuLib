/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.ui;

import net.emustudio.emulib.runtime.helpers.StringUtils;
import net.jcip.annotations.NotThreadSafe;

import java.awt.*;
import java.util.Objects;
import java.util.function.Function;

/**
 * A shortened string from a value. A string computed from given value can be shortened to fit the component width.
 *
 * @param <T> type of the value
 */
@NotThreadSafe
public class ShortenedString<T> {
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
     * Should be called when the component width changes.
     *
     * @param component      component
     * @param componentWidth custom component width (if it's different to Component.getWidth())
     */
    public void deriveMaxStringLength(Component component, int componentWidth) {
        FontMetrics fontMetrics = component.getFontMetrics(component.getFont());
        String fullString = getFullString();
        int fullStringWidth = fontMetrics.stringWidth(fullString);

        if (fullStringWidth <= 0) {
            this.maxStringLength = fullString.length();
            return;
        }
        this.maxStringLength = fullString.length() * Math.min(componentWidth, fullStringWidth) / fullStringWidth;
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
     * suffix, and inserts "..." in the middle.
     *
     * @return shortened string
     */
    public String getShortenedString() {
        return StringUtils.shorten(toString.apply(value), maxStringLength);
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
