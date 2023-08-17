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
package net.emustudio.emulib.runtime.interaction;

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
