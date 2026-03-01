/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime;

/**
 * Raised when a plugin tries to register a context which is already registered.
 */
public class ContextAlreadyRegisteredException extends Exception {

    /**
     * Constructs a new ContextAlreadyRegisteredException.
     */
    public ContextAlreadyRegisteredException() {
    }
}
