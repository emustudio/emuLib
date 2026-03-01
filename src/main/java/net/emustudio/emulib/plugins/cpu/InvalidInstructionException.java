/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.cpu;

/**
 * An exception representing an unsuccessful decoding process.
 */
@SuppressWarnings("unused")
public class InvalidInstructionException extends Exception {

    /**
     * Constructs a new InvalidInstructionException.
     */
    public InvalidInstructionException() {
    }

    /**
     * Constructs a new InvalidInstructionException.
     *
     * @param message the detail message
     */
    public InvalidInstructionException(String message) {
        super(message);
    }
}
