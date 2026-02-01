/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.memory.annotations;

import net.jcip.annotations.ThreadSafe;

/**
 * Memory annotations.
 * <p>
 * Each memory cell (at one location) can be annotated with one or more annotations. An annotation is a class extending
 * {@link net.emustudio.emulib.plugins.memory.annotations.Annotation} abstract class, which must implement hashCode and
 * equals methods properly. That way it is guaranteed multiple annotations can be put on single location, but they won't
 * get duplicated.
 * <p>
 * Accessible from memory context, but some methods ({@link #clear()}, {@link #removeAll(Class)}) are needed by the root
 * memory class. The reason for not allowing to ignore source plugin ID in the context is to be able to forbid by one
 * plugin to remove annotations of another plugin. The root memory plugin class is accessible only to emuStudio, which
 * can ultimately decide about all annotations since it controls all plugins already.
 */
@ThreadSafe
public interface MemoryAnnotations extends MemoryContextAnnotations {

    /**
     * Clears all annotations
     */
    void clear();

    /**
     * Removes all annotations of given annotation class
     *
     * @param annotationClass annotation class
     */
    void removeAll(Class<?> annotationClass);
}
