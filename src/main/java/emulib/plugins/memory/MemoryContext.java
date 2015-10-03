/*
 * KISS, YAGNI, DRY
 *
 * (c) Copyright 2008-2014, Peter Jakubčo
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package emulib.plugins.memory;

import emulib.annotations.ContextType;
import emulib.plugins.Context;
import emulib.plugins.memory.Memory.MemoryListener;

/**
 * Interface provides a context for operating memory. It supports basic methods,
 * but if memory wants to support more functionality, this interface should be
 * extended by plugin programmer and he should then make it public, in order to
 * plugins have access to it.
 *
 * The context is given to plugins (compiler, CPU, devices), that are connected
 * to the memory and they communicate by invoking following methods.
 */
@ContextType
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
     * <code>
     *     result = (mem[from]&0xFF) | ((mem[from+1]<<8)&0xFF);
     * </code>
     *
     * and in big endian as:
     * <code>
     *     result = ((mem[from]<<8)&0xFF) | (mem[from+1]&0xFF);
     * </code>
     *
     * @param memoryPosition  memory position (address) of the read cells
     * @return two read cells, accessible at indexes 0 and 1, respectively.
     */
    CellType[] readWord(int memoryPosition);

    /**
     * Write one cell-size (e.g. byte) data to a cell to a memory at specified location.
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

}

