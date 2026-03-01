/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.memory.annotations;

import net.jcip.annotations.Immutable;

import java.util.Objects;

/**
 * Text annotation.
 * <p>
 * A memory cell can be described with any text.
 */
@Immutable
public class TextAnnotation extends Annotation {
    /**
     * The annotation text.
     */
    private final String info;

    /**
     * Constructs a new TextAnnotation.
     *
     * @param sourcePluginId source plugin ID
     * @param text           annotation text
     */
    public TextAnnotation(long sourcePluginId, String text) {
        super(sourcePluginId);
        this.info = Objects.requireNonNull(text);
    }

    /**
     * Gets the annotation text.
     *
     * @return annotation text
     */
    public String getText() {
        return info;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TextAnnotation that = (TextAnnotation) o;
        return info.equals(that.info);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), info);
    }
}
