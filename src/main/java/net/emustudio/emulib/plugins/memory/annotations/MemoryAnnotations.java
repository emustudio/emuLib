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
