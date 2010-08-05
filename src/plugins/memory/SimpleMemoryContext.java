/*
 * SimpleMemoryContext.java
 *
 * (c) Copyright 2010, P.Jakubčo <pjakubco@gmail.com>
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

import java.util.EventObject;
import javax.swing.event.EventListenerList;
import plugins.memory.IMemory.IMemListener;

/**
 * This class implements some fundamental functionality of IMemoryContext
 * interface, that can be useful in the programming of the own memory context.
 *
 * @author vbmacher
 */
public abstract class SimpleMemoryContext implements IMemoryContext {
    /**
     * List of all memory listeners. The listeners are objects implementing
     * the IMemoryListener interface. Methods within the listeners are called
     * on some events that happen inside memory (e.g. value change).
     */
    protected EventListenerList listeners;

    /**
     * Event object
     */
    protected EventObject changeEvent;

    /**
     * Public constructor initializes listeners list and event object for
     * event passing.
     */
    public SimpleMemoryContext() {
        changeEvent = new EventObject(this);
        listeners = new EventListenerList();
    }

    /**
     * Adds a listener onto listeners list
     *
     * @param listener listener object
     */
    @Override
    public void addMemoryListener(IMemListener listener) {
        listeners.add(IMemListener.class, listener);
    }

    /**
     * Removes the listener from listeners list
     *
     * @param listener listener object
     */
    @Override
    public void removeMemoryListener(IMemListener listener) {
        listeners.remove(IMemListener.class, listener);
    }

    /**
     * This method notifies all listeners that the memory cell value changed
     * on specific location (memory address).
     *
     * This method should be called whenever a some plug-in writes to the
     * memory.
     *
     * @param adr memory location (address), value of that changed
     */
    public void fireChange(int adr) {
        Object[] listenersList = listeners.getListenerList();
        for (int i = listenersList.length-2; i>=0; i-=2) {
            if (listenersList[i]==IMemListener.class) {
                ((IMemListener)listenersList[i+1]).memChange(changeEvent, adr);
            }
        }
    }

}
