/*
 * Run-time library for emuStudio and plugins.
 *
 *     Copyright (C) 2006-2020  Peter Jakubčo
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.emustudio.emulib.plugins.device;

import net.emustudio.emulib.plugins.annotations.PluginRoot;
import net.emustudio.emulib.runtime.PluginSettings;
import net.emustudio.emulib.runtime.PluginInitializationException;
import java.util.Objects;

/**
 * Abstract device class. Implements some fundamental methods.
 *
 */
public abstract class AbstractDevice implements Device {
    /**
     * plugin identification number
     */
    protected final long pluginID;

    /**
     * Initializes this AbstractDevice. Only assigns the plugin ID into
     * the class field.
     *
     * @param pluginID plugin id
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
    public void initialize(PluginSettings settings) throws PluginInitializationException {

    }

    @Override
    public String getTitle() {
        return getClass().getAnnotation(PluginRoot.class).title();
    }

    /**
     * No-operation. Should be overridden if needed.
     */
    @Override
    public void reset() {

    }

}
