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
     * Gets size of memory.
     * <p>
     * If memory uses some techniques as banking, real
     * size of the memory is not computed. It is only returned a value set
     * in architecture configuration.
     *
     * @return basic size of the memory
     */
    int getSize();

    @Override
    default boolean isAutomationSupported() {
        return true;
    }
}

