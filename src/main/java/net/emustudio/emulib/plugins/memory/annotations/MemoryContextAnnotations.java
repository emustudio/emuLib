/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.memory.annotations;

import net.jcip.annotations.ThreadSafe;

import java.util.*;

/**
 * Annotations for memory context.
 */
@ThreadSafe
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
