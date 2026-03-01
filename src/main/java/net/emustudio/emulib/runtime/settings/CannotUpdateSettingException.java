/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.settings;

@SuppressWarnings("unused")
/**
 * Exception thrown when a setting cannot be updated.
 */
public class CannotUpdateSettingException extends RuntimeException {
    /**
     * Constructs a new CannotUpdateSettingException.
     *
     * @param message the detail message
     */
    public CannotUpdateSettingException(String message) {
        super(message);
    }

    /**
     * Constructs a new CannotUpdateSettingException.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public CannotUpdateSettingException(String message, Throwable cause) {
        super(message, cause);
    }
}
