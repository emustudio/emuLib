/*
 * IConnections.java
 * 
 * KISS, YAGNI
 *
 * Copyright (C) 2009-2010 Peter Jakubčo <pjakubco at gmail.com>
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

package runtime.interfaces;

/**
 * This interface includes methods for retrieving computer structure -
 * plug-in interconnection.
 *
 * @author vbmacher
 */
public interface IConnections {

    public static final int TYPE_CPU = 0;
    public static final int TYPE_COMPILER = 1;
    public static final int TYPE_MEMORY = 2;
    public static final int TYPE_DEVICE = 3;
    public static final int TYPE_UNKNOWN = 4;

    /**
     * Get type of given plug-in.
     *
     * @param pluginID plug-in ID
     * @return one of the value TYPE_CPU, TYPE_COMPILER, TYPE_MEMORY or
     *         TYPE_DEVICE
     */
    public int getPluginType(long pluginID);

    /**
     * Check if two plug-ins are connected within the abstract schema.
     *
     * If it is said that "plugin1 is connected to plugin2", it means that
     * the plugin1 should know plugin2, but this is not holding for the opposite
     * direction.
     *
     * This method checks, if pluginID is connected to toPluginID.
     *
     * @param pluginID
     * @param toPluginID
     * @return true if pluginID is connected to withPluginID.
     */
    public boolean isConnected(long pluginID, long toPluginID);

}
