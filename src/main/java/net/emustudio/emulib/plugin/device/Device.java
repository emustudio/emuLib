/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugin.device;

import net.emustudio.emulib.plugin.Plugin;

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
     * Called only if {@link #isGuiSupported()} returns true.
     *
     * @param parent emuStudio main window
     */
    default void showGUI(JFrame parent) {
    }

    /**
     * Returns true if this device has GUI.
     *
     * @return true if GUI is supported; false otherwise
     */
    default boolean isGuiSupported() {
        return false;
    }
}

