/*
 * KISS, YAGNI, DRY
 *
 * Copyright (C) 2006-2016, Peter Jakubčo
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

package emulib.plugins.device;

import emulib.annotations.PluginType;
import emulib.emustudio.SettingsManager;
import emulib.runtime.exceptions.PluginInitializationException;
import java.util.Objects;

/**
 * Abstract device class. Implements some fundamental methods.
 *
 */
public abstract class AbstractDevice implements Device {
    /**
     * Plug-in identification number
     */
    protected final long pluginID;

    /**
     * Initializes this AbstractDevice. Only assigns the plug-in ID into
     * the class field.
     *
     * @param pluginID plug-in id
     * @throws NullPointerException if pluginID is null
     */
    public AbstractDevice(Long pluginID) {
        this.pluginID = Objects.requireNonNull(pluginID);
    }

    /**
     * No-operation. Should be overridden if needed.
     *
     * @param settings settings manipulation object
     * @throws PluginInitializationException never in the default implementation
     */
    @Override
    public void initialize(SettingsManager settings) throws PluginInitializationException {

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
