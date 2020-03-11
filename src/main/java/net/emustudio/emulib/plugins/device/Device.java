// SPDX-License-Identifier: GPL-3.0-or-later
package net.emustudio.emulib.plugins.device;

import net.emustudio.emulib.plugins.Plugin;

/**
 * Device plugin root interface.
 *
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
     *
     * In case GUI is not supported, it should do nothing.
     */
    void showGUI ();

}

