/*
 * KISS, YAGNI, DRY
 *
 * (c) Copyright 2006-2016, Peter Jakubčo
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
import emulib.runtime.exceptions.PluginInitializationException;
import java.util.Objects;

/**
 * Abstract memory. It implements some fundamental methods which are usually shared across the most of memories.
 */
public abstract class AbstractMemory implements Memory {
    /**
     * Start location of loaded program. This variable is changed by compiler (mostly).
     */
    private int programStart;

    /**
     * ID of the plug-in assigned by emuStudio
     */
    protected final long pluginID;

    /**
     * Sets up plug-in id.
     *
     * @param pluginID plug-in identification number
     * @throws NullPointerException if pluginID is null
     */
    public AbstractMemory(Long pluginID) {
        this.pluginID = Objects.requireNonNull(pluginID);
    }

    /**
     * No-operation. Should be overridden if needed.
     *
     * @param settings settings manipulation object
     * @throws PluginInitializationException never in the default implementation
     */
    @Override
    public void initialize(SettingsManager settings)  throws PluginInitializationException {

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

    @Override
    public String getTitle() {
        return getClass().getAnnotation(PluginType.class).title();
    }

    /**
     * No-operation. Should be overridden if needed.
     */
    @Override
    public void reset() {

    }

}
