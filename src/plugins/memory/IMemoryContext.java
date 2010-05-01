/**
 * IMemoryContext.java
 * 
 * (c) Copyright 2008-2009, P.Jakubčo <pjakubco@gmail.com>
 * 
 * KISS,YAGNI
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
package plugins.memory;

import java.util.EventListener;
import java.util.EventObject;
import plugins.IContext; 

/**
 * Interface provides a context for operating memory. It supports basic methods,
 * but if memory wants to support more functionality, this interface should be
 * extended by plugin programmer and he should then make it public, in order to
 * plugins have access to it.
 * 
 * The context is given to plugins (compiler, CPU, devices), that are connected
 * to the memory and they communicate by invoking following methods.
 */
public interface IMemoryContext extends IContext {

    /**
     * Read one cell from a memory.
     * @param from  memory position (address) of the read cell
     * @return read cell (don't have to be a byte, therefore its just
     *         <code>Object</code>)
     */
    public Object read (int from);

    /**
     * Read two cells from a memory at once. Implementation of return value
     * is up to plugin programmer (concatenation of the cells). If cells in
     * memory are pure bytes (java type is e.g. <code>short</code>), concatenation
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
     * @param from  memory position (address) of the read cells
     * @return two read cells
     */
    public Object readWord (int from);

    /**
     * Write one cell-size (e.g. byte) data to a cell to a memory on specified location.
     * @param to   memory position (address) of the cell where data will be written
     * @param val  data to be written
     */
    public void write (int to, Object val);

    /**
     * Write two cell-size (e.g. word or two bytes) data to a cell to a memory on specified
     * location. Implementation of data value is up to plugin programmer
     * (concatenation of the cells) and have to be understandable by memory.
     * @param to   memory position (address) of the read cells
     * @param val  two cells in one <code>Object</code> value
     */
    public void writeWord (int to, Object val);

    /**
     * Get the type of transferred data. As you can see, methods <code>read</code> and
     * <code>write</code> use <code>Object</code> as the data type. This method should
     * make the data type specific.
     * @return data type of transferred data
     */
    public Class getDataType ();
    
    /**
     * Clears the memory.
     */
    public void clearMemory ();
    
    /**
     * Adds the specified memory listener to receive memory events from this memory.
     * Memory events occur even if single cell is changed in memory.
     * If listener is <code>null</code>, no exception is thrown and no action is
     * performed.
     * @param listener  the memory listener
     */
    public void addMemoryListener (IMemoryContext.IMemListener listener);

    /**
     * Removes the specified memory listener so that it no longer receives memory
     * events from this memory. Memory events occur even if single cell is
     * changed in memory. If listener is <code>null</code>, no exception is 
     * thrown and no action is performed.
     * @param listener  the memory listener to be removed
     */
    public void removeMemoryListener (IMemoryContext.IMemListener listener);

    /**
     * The listener interface for receiving memory events. The class that is
     * interested in processing a memory event implements this interface, and the
     * object created with that class is registered with a memory, using the
     * memory's <code>addMemoryListener</code> method. Memory events occur even
     * if single cell is changed in memory and then is invoked
     * <code>memChange</code> method.
     */
    public interface IMemListener extends EventListener {
        /**
         * This method is invoked when memory event is occurred - when a single
         * cell is changed.
         * @param evt  event object
         * @param adr  memory position (address) of changed cell
         */
        public void memChange (EventObject evt, int adr);

    }

}

