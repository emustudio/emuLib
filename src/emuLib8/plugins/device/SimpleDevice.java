/**
 * SimpleDevice.java
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

package emuLib8.plugins.device;

import emuLib8.plugins.ISettingsHandler;

/**
 * Simple device.
 *
 * @author vbmacher
 */
public abstract class SimpleDevice implements IDevice {
    /**
     * Plug-in identification number
     */
    protected long pluginID;

    /**
     * Settings manipulation object
     */
    protected ISettingsHandler settings;

    /**
     * Initializes this SimpleDevice. Only assigns the plug-in ID into
     * the class field.
     *
     * @param pluginID
     */
    public SimpleDevice(Long pluginID) {
        this.pluginID = pluginID;
    }

    /**
     * Initializes this SimpleDevice. Only assigns the settings to the
     * class field.
     *
     * @param settings settings manipulation object
     * @return true
     */
    @Override
    public boolean initialize(ISettingsHandler settings) {
        this.settings = settings;
        return true;
    }

    /**
     * Does nothing.
     */
    @Override
    public void reset() {

    }

}
