/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
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
    /**
     * Owner plugin ID.
     */
    protected final long pluginId; // owner

    /**
     * Creates new memory annotation.
     *
     * @param pluginId owner plugin ID
     */
    public Annotation(long pluginId) {
        this.pluginId = pluginId;
    }

    /**
     * Get owner plugin ID.
     *
     * @return owner plugin ID (0 for emuStudio)
     */
    public long getPluginId() {
        return pluginId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Annotation that = (Annotation) o;
        return pluginId == that.pluginId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginId);
    }
}
