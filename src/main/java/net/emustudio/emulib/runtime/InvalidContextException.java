/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime;

import net.emustudio.emulib.plugins.PluginInitializationException;

/**
 * Raised when a plugin context does not fulfill the requirements.
 */
@SuppressWarnings("unused")
public class InvalidContextException extends PluginInitializationException {

    /**
     * Constructs a new InvalidContextException.
     *
     * @param message the detail message
     */
    public InvalidContextException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidContextException.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public InvalidContextException(String message, Throwable cause) {
        super(message, cause);
    }
}
