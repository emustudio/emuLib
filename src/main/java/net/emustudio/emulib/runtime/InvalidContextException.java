// SPDX-License-Identifier: GPL-3.0-or-later
package net.emustudio.emulib.runtime;

import net.emustudio.emulib.plugins.PluginInitializationException;

/**
 * Raised when a plugin context does not fulfill the requirements.
 */
@SuppressWarnings("unused")
public class InvalidContextException extends PluginInitializationException {

    public InvalidContextException(String message) {
        super(message);
    }

    public InvalidContextException(String message, Throwable cause) {
        super(message, cause);
    }
}
