// SPDX-License-Identifier: GPL-3.0-or-later
package net.emustudio.emulib.plugins.memory;

import net.emustudio.emulib.plugins.Plugin;

/**
 * Memory plugin root interface.
 * <p>
 * Memory can define a "memory context", which can enable additional non-standard functionality, which can be used
 * by other plugins.
 *
 * @see MemoryContext
 */
@SuppressWarnings("unused")
public interface Memory extends Plugin {

    /**
     * The listener interface for receiving memory related events.
     * <p>
     * The class that is interested in processing a memory event implements this interface, and the object created with
     * that class is registered with a memory, using the memory's <code>addMemoryListener</code> method. Memory events
     * occur even if single cell is changed in memory and then is invoked <code>memChange</code> method.
     */
    interface MemoryListener {
        /**
         * Invoked when a single memory cell is changed.
         *
         * @param memoryPosition memory position (address) of changed cell
         */
        void memoryChanged(int memoryPosition);

        /**
         * Some memories can be dynamic-sized. This method is invoked when memory size has changed.
         */
        void memorySizeChanged();

    }

    /**
     * Sets program start address.
     * <p>
     * This method is called by main module when
     * compiler finishes compilation process and return known start address of
     * compiled program. This start address is then used by CPU, in reset
     * operation - PC (program counter, or something similar) should be set
     * to this address, accessible via <code>IMemoryContext.getProgramStart()</code>
     * method.
     *
     * @param location starting memory position (address) of a program
     */
    void setProgramLocation(int location);

    /**
     * Gets size of memory.
     * <p>
     * If memory uses some techniques as banking, real
     * size of the memory is not computed. It is only returned a value set
     * in architecture configuration.
     *
     * @return basic size of the memory
     */
    int getSize();

    /**
     * Gets program's start address.
     * <p>
     * The start address is set invoking memory's method <code>Memory.setProgramStart()</code> by main module
     * when compiler finishes compilation process of a program and if the compiler know the starting address.
     * This address is used by main module for CPU reset process.
     *
     * @return program's start address in memory
     */
    int getProgramLocation();
}

