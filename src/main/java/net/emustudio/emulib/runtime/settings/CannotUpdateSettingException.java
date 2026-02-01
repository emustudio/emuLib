/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.settings;

@SuppressWarnings("unused")
public class CannotUpdateSettingException extends RuntimeException {
    public CannotUpdateSettingException(String message) {
        super(message);
    }

    public CannotUpdateSettingException(String message, Throwable cause) {
        super(message, cause);
    }
}
