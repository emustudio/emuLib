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

import java.util.*;

/**
 * Annotations for memory context.
 */
public interface MemoryContextAnnotations {

    /**
     * Removes all annotations owned by given pluginId.
     * <p>
     * Each plugin which sets memory annotations is responsible for cleaning them.
     *
     * @param pluginId plugin ID owning the annotations
     */
    void removeAll(long pluginId);

    /**
     * Removes all annotations at given memory location owned by given pluginId
     *
     * @param pluginId plugin ID owning the annotations
     * @param location memory location
     */
    void removeAll(long pluginId, int location);

    /**
     * Get all annotations of given type
     * <p>
     * Consider annotations of all plugin IDs
     *
     * @param annotationClass annotation class (of type T)
     * @param <T>             type of annotation
     * @return annotations of type T (always non-null)
     */
    <T extends Annotation> Map<Integer, Set<T>> getAll(Class<? extends T> annotationClass);

    /**
     * Get all annotations of given type owned by given pluginId
     *
     * @param pluginId        plugin ID owning the annotations
     * @param annotationClass annotation class (of type T)
     * @param <T>             type of annotation
     * @return annotations of type T owned by given plugin ID (always non-null)
     */
    <T extends Annotation> Map<Integer, Set<T>> getAll(long pluginId, Class<? extends T> annotationClass);

    /**
     * Get all annotations owned by given plugin ID
     *
     * @param pluginId plugin ID owning the annotations
     * @return annotations coming from given source plugin ID (always non-null)
     */
    Map<Integer, Set<Annotation>> getAll(long pluginId);

    /**
     * Get annotations of given type at given memory location
     *
     * @param location        memory location
     * @param annotationClass annotation class (of type T)
     * @param <T>             type of annotation
     * @return annotations set at given memory location (always non-null)
     */
    <T extends Annotation> Set<T> get(int location, Class<? extends T> annotationClass);

    /**
     * Get annotations of given type at given memory location owned by given plugin ID
     *
     * @param pluginId        plugin ID owning the annotations
     * @param location        memory location
     * @param annotationClass annotation class (of type T)
     * @param <T>             type of annotation
     * @return annotations of given type at given memory location owned by given plugin ID (always non-null)
     */
    <T extends Annotation> Set<T> get(long pluginId, int location, Class<? extends T> annotationClass);

    /**
     * Get annotations at given memory location owned by given plugin ID
     *
     * @param pluginId plugin ID owning the annotations
     * @param location memory location
     * @return annotations at given memory location owned by given plugin ID (always non-null)
     */
    Set<Annotation> get(long pluginId, int location);

    /**
     * Adds annotation at given memory location.
     * <p>
     * The annotation will be owned by plugin ID in the annotation itself.
     * <p>
     * The same annotations will not be duplicated.
     *
     * @param location   memory location
     * @param annotation annotation
     */
    void add(int location, Annotation annotation);
}
