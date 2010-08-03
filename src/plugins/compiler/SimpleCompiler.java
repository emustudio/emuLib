/*
 * SimpleCompiler.java
 *
 * KISS, YAGNI
 *
 * Copyright (C) 2010 Peter Jakubčo <pjakubco at gmail.com>
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

package plugins.compiler;

import plugins.ISettingsHandler;

/**
 * This class implements some fundamental functionality that can be
 * useful within the implementation of own compiler plug-ins.
 *
 * @author vbmacher
 */
public abstract class SimpleCompiler implements ICompiler {

    /**
     * Settings manipulation object
     */
    protected ISettingsHandler settings;

    /**
     * Program start address (memory location)
     */
    protected int programStart = 0; // actualize after compile

    /**
     * Identification number of this plug-in assigned by emuStudio
     */
    protected long pluginID;

    /**
     * This method semi-initializes the simple compiler. It only
     * set-up data members - pluginID and ISettingsHandler object.
     *
     * It should be overriden.
     *
     * @param pluginID ID of the compiler plug-in assigned by emuStudio
     * @param sHandler settings manipulation object
     * @return true
     */
    @Override
    public boolean initialize(long pluginID, ISettingsHandler sHandler) {
        this.settings = sHandler;
        this.pluginID = pluginID;
        return true;
    }

    /**
     * Returns program start address (memory location) after the compilation
     * process. If the compile process was not ran, it will return 0.
     * 
     * @return program start address (memory location)
     */
    @Override
    public int getProgramStartAddress() {
        return programStart;
    }

}
