/**
 * IDevice.java
 * 
 * (c) Copyright 2008-2010, P. Jakubčo <pjakubco@gmail.com>
 * 
 * KISS, YAGNI
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
package emulib8_0.plugins.device;

import emulib8_0.plugins.IPlugin;

/**
 * Main interface that has to be implemented by device plugin.
 *
 * Design of the interface supports hierarchical connection of devices. Each
 * device can implement one or more device contexts. The contexts can be
 * identified by implemented interface, and/or the identification number (ID).
 * 
 */
public interface IDevice extends IPlugin {

    /**
     * Shows GUI of a device. Device don't have to have a GUI, but instead it
     * should display information message. 
     */
    public void showGUI ();
    
}

