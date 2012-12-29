/*
 * AbstractMemory.java
 *
 * KISS, YAGNI, DRY
 * 
 * (c) Copyright 2010-2012, Peter Jakubčo
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

import emulib.annotations.PluginType;
import emulib.emustudio.SettingsManager;
import java.util.ArrayList;
import java.util.List;

/**
 * This class implements some fundamental functionality that can be shared
 * by most memory implementations.
 *
 * @author vbmacher
 */
public abstract class AbstractMemory implements Memory {
    /**
     * List of all memory listeners. The listeners are objects implementing
     * the IMemoryListener interface. Methods within the listeners are called
     * on some events that happen inside memory (e.g. value change).
     */
    protected List<MemoryListener> listeners;

    /**
     * Start location of loaded program. This variable is changed by
     * compiler (mostly).
     */
    protected int programStart;

    /**
     * ID of the plug-in assigned by emuStudio
     */
    protected long pluginID;

    /**
     * Settings manipulation object
     */
    protected SettingsManager settings;

    /**
     * Public constructor initializes listeners list and event object for
     * event passing.
     *
     * @param pluginID plug-in identification number
     */
    public AbstractMemory(Long pluginID) {
        listeners = new ArrayList<MemoryListener>();
        this.pluginID = pluginID;
    }

    /**
     * This method does a semi-initialization of the memory. It loads
     * variables: pluginID and SettingsManager object.
     *
     * It is called by emuStudio in the initialization process. Should
     * be overriden.
     *
     * @param sHandler settings manipulation object
     * @return true
     */
    @Override
    public boolean initialize(SettingsManager sHandler) {
        this.settings = sHandler;
        return true;
    }

    /**
     * Get program starting address (memory location), as it was loaded by
     * the method setProgramStart().
     *
     * @return program starting address (memory location)
     */
    @Override
    public int getProgramStart() {
    	return programStart;
    }

    /**
     * Set program starting address (memory location). Mostly it is called
     * by the emuStudio after the compiler returns. When the compiler
     * compiles the source, the emuStudio gets compiled program starting
     * address (if unknown, it will be 0) and pass it here.
     *
     * @param address program starting address (memory location)
     */
    @Override
    public void setProgramStart(int address) {
        programStart = address;
    }

    /**
     * Adds a listener onto listeners list
     *
     * @param listener listener object
     */
    @Override
    public void addMemoryListener(MemoryListener listener) {
        listeners.add(listener);
    }
    
    @Override
    public String getTitle() {
        return getClass().getAnnotation(PluginType.class).title();
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
     * This method notifies all listeners that the memory cell value changed
     * on specific location (memory address).
     *
     * This method should be called whenever a some plug-in writes to the
     * memory.
     *
     * @param adr memory location (address), value of that changed
     */
    public void fireChange(int adr) {
        for (MemoryListener listener : listeners) {
            listener.memoryChanged(adr);
        }
    }

    /**
     * Does nothing.
     */
    @Override
    public void reset() {

    }

}
