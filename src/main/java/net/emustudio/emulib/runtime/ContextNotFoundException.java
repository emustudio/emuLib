// SPDX-License-Identifier: GPL-3.0-or-later
package net.emustudio.emulib.runtime;

import net.emustudio.emulib.plugins.PluginInitializationException;

/**
 * Raised when a plugin context is not registered in the context pool.
 */
public class ContextNotFoundException extends PluginInitializationException {

    public ContextNotFoundException(String message) {
        super(message);
    }

}
