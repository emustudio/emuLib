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
package net.emustudio.emulib.plugins.memory;

import net.emustudio.emulib.plugins.Context;
import net.emustudio.emulib.plugins.annotations.PluginContext;
import net.emustudio.emulib.plugins.memory.annotations.Annotations;

/**
 * Memory context
 * <p>
 * It provides the basic memory functionality, such as reading/writing memory cells. The memory cell can be of any Java
 * type, thus a "byte", as the lowest addressable unit, is not enforced.
 * <p>
 * The memory context must be extended in a memory plugin in order to support additional functionality (e.g. bank-switching
 * or memory-mapped I/O).
 * <p>
 * Memory context supports annotations - a possibility to annotate any memory cell with supplemental information. Usual
 * annotations are: breakpoints, text information, position in source code, or any custom annotation. Annotation API
 * is accessible by using {@link #annotations()} method.
 *
 * @param <CellType> memory cell type
 */
@SuppressWarnings("unused")
@PluginContext
public interface MemoryContext<CellType> extends Context {

    /**
     * A listener for receiving memory related events.
     * <p>
     * A class interested in processing memory event implements this interface, and the object created with
     * that class is registered with the memory context (with {@link #addMemoryListener(MemoryListener)} method).
     */
    interface MemoryListener {
        /**
         * Invoked when memory content changed.
         *
         * @param fromLocation from location
         * @param toLocation   to location
         */
        void memoryContentChanged(int fromLocation, int toLocation);

        /**
         * Invoked when memory size changed
         */
        void memorySizeChanged();
    }

    /**
     * Reads one memory cell at given location.
     *
     * @param location memory location of the memory cell
     * @return cell at given location
     */
    CellType read(int location);

    /**
     * Reads one or more adjacent cells from a memory.
     * <p>
     * Implementation of return value is up to plugin programmer (e.g. ordering of cells or atomicity).
     * If cells in memory are pure bytes, concatenation can be realized as (in small endian):
     *
     * <pre>
     * {@code
     * result = (mem[from]&0xFF) | ((mem[from+1]<<8)&0xFF) | ...;
     * }
     * </pre>
     * <p>
     * and in big endian as:
     *
     * <pre>
     * {@code
     * result = ((mem[from]<<(count *8 ))&0xFF) | (mem[from+1]<<((count-1)*8)&0xFF) | ...;
     * }
     * </pre>
     * <p>
     * If memory size is smaller than (location + count), then only available cells are returned - returned
     * array size can be less than <code>count</code>.
     *
     * @param location location of the first cell
     * @param count    number of cells to read
     * @return array of cells at given location
     * @throws RuntimeException if memory size is smaller than (location + count)
     */
    CellType[] read(int location, int count);

    /**
     * Write a value to a memory cell at given location.
     *
     * @param location location of the cell
     * @param value    data to be written
     */
    void write(int location, CellType value);

    /**
     * Write several cell values at once to a given location.
     * The ordering of values, as well as atomicity is implementation-specific.
     *
     * @param location location of the first cell
     * @param values   data to be written
     * @param count    number of values taken from given array
     * @throws RuntimeException if memory size is less than (location + count) or if (count &gt; values.length) or if (count &lt; 0)
     */
    void write(int location, CellType[] values, int count);

    /**
     * Write several cell values at once to a given location.
     * The ordering of values, as well as atomicity is implementation-specific.
     *
     * @param location location of the first cell
     * @param values   data to be written
     * @throws RuntimeException if memory size is less than (location + count)
     */
    default void write(int location, CellType[] values) {
        write(location, values, values.length);
    }

    /**
     * Get memory cell type class.
     *
     * @return Java data type of memory cells
     */
    Class<CellType> getCellTypeClass();

    /**
     * Clears the memory content.
     */
    void clear();

    /**
     * Get memory size
     * <p>
     * A memory size is basically a number of cells.
     * <p>
     * If the memory supports bank-switching, it is up to the implementing memory how this is interpreted. Usually
     * it returns size of the active (alternatively the most "common") bank.
     *
     * @return memory size (number of cells)
     */
    int getSize();

    /**
     * Adds a memory listener
     *
     * @param listener the memory listener (non-null)
     */
    void addMemoryListener(MemoryListener listener);

    /**
     * Removes a memory listener
     *
     * @param listener the memory listener to be removed (non-null)
     */
    void removeMemoryListener(MemoryListener listener);

    /**
     * Enable/disable notifications of memory changes globally.
     * <p>
     * Enabled by default. Usually it is disabled by CPU when in a "running" state.
     *
     * @param enabled - true if enabled, false if disabled.
     */
    void setMemoryNotificationsEnabled(boolean enabled);

    /**
     * Determine if notifications of memory changes are globally enabled or disabled.
     *
     * @return true if notifications are enabled, false if disabled.
     */
    boolean areMemoryNotificationsEnabled();

    /**
     * Get memory annotations API.
     * <p>
     * Annotations is a possibility to annotate any memory cell with supplemental information. Usual
     * annotations are: breakpoints, text information, position in source code, or any custom annotation.
     *
     * @return memory annotations API
     */
    Annotations annotations();
}

