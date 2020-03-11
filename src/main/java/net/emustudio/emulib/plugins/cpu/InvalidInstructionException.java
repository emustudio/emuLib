// SPDX-License-Identifier: GPL-3.0-or-later
package net.emustudio.emulib.plugins.cpu;

/**
 * An exception representing an unsuccessful decoding process.
 */
@SuppressWarnings("unused")
public class InvalidInstructionException extends Exception {

    public InvalidInstructionException() {
    }

    public InvalidInstructionException(String message) {
        super(message);
    }
}
