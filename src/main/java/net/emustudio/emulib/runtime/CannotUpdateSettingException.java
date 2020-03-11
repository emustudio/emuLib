// SPDX-License-Identifier: GPL-3.0-or-later
package net.emustudio.emulib.runtime;

@SuppressWarnings("unused")
public class CannotUpdateSettingException extends Exception {
    public CannotUpdateSettingException(String message) {
        super(message);
    }

    public CannotUpdateSettingException(String message, Throwable cause) {
        super(message, cause);
    }
}
