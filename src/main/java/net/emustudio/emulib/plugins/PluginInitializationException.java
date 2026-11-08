/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins;

/**
 * Exception representing general plugin initialization error.
 */
public class PluginInitializationException extends Exception {

    /**
     * @param message the detail message
     */
    public PluginInitializationException(String message) {
        super(message);
    }

    /**
     * @param message the detail message
     * @param cause   the cause
     */
    public PluginInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause the cause
     */
    public PluginInitializationException(Throwable cause) {
        super(cause);
    }
}
