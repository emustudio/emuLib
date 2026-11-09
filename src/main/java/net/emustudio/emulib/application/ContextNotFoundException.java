/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.application;

import net.emustudio.emulib.plugin.PluginInitializationException;

/**
 * Raised when a plugin context is not registered in the context pool.
 */
public class ContextNotFoundException extends PluginInitializationException {

    /**
     * Constructs a new ContextNotFoundException.
     *
     * @param message the detail message
     */
    public ContextNotFoundException(String message) {
        super(message);
    }

}
