/*
 * This file is part of emuLib.
 *
 * Copyright (C) 2006-2020  Peter Jakubčo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.emustudio.emulib.plugins.device;

import net.emustudio.emulib.plugins.Plugin;

import javax.swing.*;

/**
 * Device plugin root interface.
 * <p>
 * Should be implemented by a plugin. There should exist just one implementation.
 * When a device has more physical interfaces which can accept connections, each of them should be implemented by
 * own {@link DeviceContext}.
 *
 * @see DeviceContext
 */
@SuppressWarnings("unused")
public interface Device extends Plugin {

    /**
     * Shows GUI of the device.
     * <p>
     * In case GUI is not supported, it should do nothing.
     *
     * @param parent emuStudio main window
     */
    void showGUI(JFrame parent);

}

