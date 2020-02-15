/*
 * Run-time library for emuStudio and plug-ins.
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

import net.emustudio.emulib.plugins.Plugin;

/**
 * Main interface that has to be implemented by device plug-in.
 *
 * Design of the interface supports hierarchical connection of devices. Each
 * device can implement one or more device contexts. The contexts can be
 * identified by implemented interface, and/or the identification number (ID).
 *
 */
@SuppressWarnings("unused")
public interface Device extends Plugin {

    /**
     * Shows GUI of a device. Device don't have to have a GUI, but instead it
     * should display information message.
     */
    void showGUI ();

}

