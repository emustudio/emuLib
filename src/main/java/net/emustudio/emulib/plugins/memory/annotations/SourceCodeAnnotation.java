/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.memory.annotations;

import net.emustudio.emulib.plugins.compiler.SourceCodePosition;
import net.jcip.annotations.Immutable;

import java.util.Objects;

/**
 * Source-code position annotation.
 * <p>
 * Links a memory cell with a position in the source code.
 */
@Immutable
public class SourceCodeAnnotation extends Annotation {
    /**
     * The source code position.
     */
    private final SourceCodePosition position;

    /**
     * Constructs a new SourceCodeAnnotation.
     *
     * @param sourcePluginId source plugin ID
     * @param position       source code position
     */
    public SourceCodeAnnotation(long sourcePluginId, SourceCodePosition position) {
        super(sourcePluginId);
        this.position = Objects.requireNonNull(position);
    }

    /**
     * Gets the source code position.
     *
     * @return source code position
     */
    public SourceCodePosition getPosition() {
        return position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SourceCodeAnnotation that = (SourceCodeAnnotation) o;
        return position.equals(that.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), position);
    }
}
