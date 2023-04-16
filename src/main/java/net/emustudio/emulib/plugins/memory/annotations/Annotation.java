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

import net.jcip.annotations.Immutable;

import java.io.Serializable;
import java.util.Objects;

/**
 * Memory cell annotation.
 * The annotation contains metadata available to all plugins and emuStudio.
 * One annotation can be set to one or multiple memory locations.
 * <p>
 * All subclasses should define equals() and hashCode()
 */
@Immutable
public abstract class Annotation implements Serializable {
    protected final long sourcePluginId;

    public Annotation(long sourcePluginId) {
        this.sourcePluginId = sourcePluginId;
    }

    /**
     * Get source plugin ID (which plugin set the metadata?)
     *
     * @return source plugin ID (0 for emuStudio)
     */
    public long getSourcePluginId() {
        return sourcePluginId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Annotation that = (Annotation) o;
        return sourcePluginId == that.sourcePluginId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourcePluginId);
    }
}
