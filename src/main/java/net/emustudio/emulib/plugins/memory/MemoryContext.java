/*
 * Run-time library for emuStudio and plugins.
 *
 *     Copyright (C) 2006-2020  Peter Jakubčo
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.emustudio.emulib.plugins.memory;

import net.emustudio.emulib.plugins.annotations.PluginContext;
import net.emustudio.emulib.plugins.Context;
import net.emustudio.emulib.plugins.memory.Memory.MemoryListener;

/**
 * This memory context supports basic methods for accessing the memory, like reading and writing memory cells.
 * If the memory wants to support additional functionality, it should extend this interface.
 *
 * Plugins which need the specific memory contexts, should declare a dependency on the memory plugin.
 *
 * @param <CellType> type of the memory cell
 */
@SuppressWarnings("unused")
@PluginContext
public interface MemoryContext<CellType> extends Context {

    /**
     * Reads one cell from a memory.
     *
     * @param memoryPosition  memory position (address) of the read cell
     * @return read cell
     */
    CellType read (int memoryPosition);

    /**
     * Reads two adjacent cells from a memory at once.
     *
     * Implementation of return value is up to plugin programmer (e.g. ordering of cells).
     * If cells in memory are pure bytes (java type is e.g. <code>short</code>), concatenation
     * can be realized as (in small endian):
     *
     * <pre>
     * {@code
     * result = (mem[from]&0xFF) | ((mem[from+1]<<8)&0xFF);
     * }
     * </pre>
     *
     * and in big endian as:
     *
     * <pre>
     * {@code
     * result = ((mem[from]<<8)&0xFF) | (mem[from+1]&0xFF);
     * }
     * </pre>
     *
     * @param memoryPosition  memory position (address) of the read cells
     * @return two read cells, accessible at indexes 0 and 1, respectively.
     */
    CellType[] readWord(int memoryPosition);

    /**
     * Write one cell-size (e.g. byte) data to a cell to a memory at specified location.
     *
     * @param memoryPosition   memory position (address) of the cell where data will be written
     * @param value  data to be written
     */
    void write (int memoryPosition, CellType value);

    /**
     * Write two cell-size (e.g. word - usually two bytes) data to a cell to a memory at specified location.
     *
     * @param memoryPosition   memory position (address) of the cell with index 0
     * @param value  two cells at indexes 0 and 1, respectively.
     */
    void writeWord (int memoryPosition, CellType[] value);

    /**
     * Get the type of memory cells.
     * @return Java data type of memory cells
     */
    Class<?> getDataType ();

    /**
     * Clears the memory.
     */
    void clear ();

    /**
     * Adds the specified memory listener to receive memory events from this memory.
     * Memory events occur even if single cell is changed in memory.
     * If listener is <code>null</code>, no exception is thrown and no action is
     * performed.
     * @param listener  the memory listener
     */
    void addMemoryListener (MemoryListener listener);

    /**
     * Removes the specified memory listener so that it no longer receives memory
     * events from this memory. Memory events occur even if single cell is
     * changed in memory. If listener is <code>null</code>, no exception is
     * thrown and no action is performed.
     * @param listener  the memory listener to be removed
     */
    void removeMemoryListener (MemoryListener listener);

    /**
     * Get memory size.
     *
     * The size is a number of cells of the generic type T.
     *
     * @return memory size
     */
    int getSize();

    /**
     * Enable/disable notifications of memory changes globally.
     *
     * Enabled by default.
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

}

