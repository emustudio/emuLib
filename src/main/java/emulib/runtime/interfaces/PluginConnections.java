/*
 * KISS, YAGNI, DRY
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

package emulib.runtime.interfaces;

/**
 * The interface provides method determining plug-ins interconnections.
 *
 */
public interface PluginConnections {

    /**
     * Determine if two plug-ins are connected within the abstract schema.
     *
     * Plug-in A is connected to plug-in B when there exists a direct connection from plug-in A to plug-in B.
     * If so, plug-in A can get and use all contexts registered by plug-in B using context pool.
     * 
     * Plug-in B can get context registered by plug-in A only if the connection is bidirectional, i.e.
     * in case when also isConnected(pluginB, pluginA) holds true.
     *
     * @param pluginA
     * @param pluginB
     * @return true if pluginA is connected to pluginB.
     */
    public boolean isConnected(long pluginA, long pluginB);

}
