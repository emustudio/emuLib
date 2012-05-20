/*
 * IMemory.java
 * 
 * KISS, YAGNI, DRY
 * 
 * (c) Copyright 2008-2012, Peter Jakubčo
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

import java.util.EventListener;
import java.util.EventObject;
import emulib.plugins.IPlugin;

/**
 * This is the main interface that memory plugin should implement.
 */
public interface IMemory extends IPlugin {

    /**
     * Sets program start address. This method is called by main module when
     * compiler finishes compilation process and return known start address of
     * compiled program. This start address is then used by CPU, in reset
     * operation - PC (program counter, or something similar) should be set
     * to this address, accessible via <code>IMemoryContext.getProgramStart()</code>
     * method.
     * @param location  starting memory position (address) of a program
     */
    public void setProgramStart (int location);

    /**
     * Gets size of memory. If memory uses some techniques as banking, real
     * size of the memory is not computed. It is only returned a value set
     * in architecture configuration.
     * @return basic size of the memory
     */
    public int getSize ();

    /**
     * Gets program's start address. The start address is set invoking 
     * memory's method <code>IMemory.setProgramStart()</code> by main module
     * when compiler finishes compilation process of a program and if the compiler
     * know the starting address. This address is used by main module for
     * CPU reset process.
     * 
     * @return program's start address in memory
     */
    public int getProgramStart ();

    /**
     * Adds the specified memory listener to receive memory events from this memory.
     * Memory events occur even if single cell is changed in memory.
     * If listener is <code>null</code>, no exception is thrown and no action is
     * performed.
     * @param listener  the memory listener
     */
    public void addMemoryListener (IMemListener listener);

    /**
     * Removes the specified memory listener so that it no longer receives memory
     * events from this memory. Memory events occur even if single cell is
     * changed in memory. If listener is <code>null</code>, no exception is
     * thrown and no action is performed.
     * @param listener  the memory listener to be removed
     */
    public void removeMemoryListener (IMemListener listener);

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
         * @param location  memory position (address) of changed cell
         */
        public void memChange (EventObject evt, int location);

    }
}

