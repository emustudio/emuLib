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
    private final SourceCodePosition position;

    public SourceCodeAnnotation(long sourcePluginId, SourceCodePosition position) {
        super(sourcePluginId);
        this.position = Objects.requireNonNull(position);
    }

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
