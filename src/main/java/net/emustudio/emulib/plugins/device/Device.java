/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
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

    /**
     * Returns true if this device has GUI.
     *
     * @return true if GUI is supported; false otherwise
     */
    boolean isGuiSupported();
}

