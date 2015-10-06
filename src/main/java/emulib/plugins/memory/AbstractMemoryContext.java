/*
 * KISS, YAGNI, DRY
 *
 * (c) Copyright 2010-2014, Peter Jakubčo
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

import emulib.plugins.memory.Memory.MemoryListener;
import net.jcip.annotations.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * This class implements some fundamental functionality of MemoryContext
 * interface, that can be useful in the programming of the own memory context.
 *
 */
@ThreadSafe
public abstract class AbstractMemoryContext<ByteType> implements MemoryContext<ByteType> {
    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractMemoryContext.class);

    /**
     * List of all memory listeners. The listeners are objects implementing
     * the IMemoryListener interface. Methods within the listeners are called
     * on some events that happen inside memory (e.g. value change).
     */
    protected final Set<MemoryListener> listeners = new CopyOnWriteArraySet<>();

    /**
     * Adds a listener onto listeners list
     *
     * @param listener listener object
     */
    @Override
    public void addMemoryListener(MemoryListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes the listener from listeners list
     *
     * @param listener listener object
     */
    @Override
    public void removeMemoryListener(MemoryListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notify all listeners that memory has changed.
     *
     * This method should be called whenever a some plug-in writes to the
     * memory.
     *
     * @param position memory position (address) on which the value has changed
     */
    public void notifyMemoryChanged(int position) {
        for (MemoryListener listener : listeners) {
            try {
                listener.memoryChanged(position);
            } catch (Exception e) {
                LOGGER.error("Memory listener error", e);
            }
        }
    }

    /**
     * Notify listeners that memory size has changed.
     */
    public void notifyMemorySizeChanged() {
        System.out.println("MEM SI CHA!");
        for (MemoryListener listener : listeners) {
            try {
                listener.memorySizeChanged();
                System.out.println("I ME DI!");
            } catch (Exception e) {
                LOGGER.error("Memory listener error", e);
            }
        }
    }

}
