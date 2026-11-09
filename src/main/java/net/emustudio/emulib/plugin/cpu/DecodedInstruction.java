/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugin.cpu;

import java.util.Objects;

/**
 * Immutable facts about one successfully decoded instruction.
 * Accessors describe captured input and never read live memory.
 */
public interface DecodedInstruction {
    /** Logical instruction start, in the CPU's address units. */
    int address();

    /** Positive number of occupied addressable cells, not necessarily bytes. */
    int length();

    /** Rendered mnemonic and operands from the captured instruction. */
    String mnemonic();

    /** Captured encoding as display text, including non-byte representations. */
    String encoding();

    /** Captures immutable instruction facts without reading or retaining machine state. */
    static DecodedInstruction of(int address, int length, String mnemonic, String encoding) {
        if (address < 0) throw new IllegalArgumentException("Negative instruction address");
        if (length < 1) throw new IllegalArgumentException("Instruction length must be positive");
        Objects.requireNonNull(mnemonic);
        Objects.requireNonNull(encoding);
        return new DecodedInstruction() {
            @Override public int address() { return address; }
            @Override public int length() { return length; }
            @Override public String mnemonic() { return mnemonic; }
            @Override public String encoding() { return encoding; }
        };
    }
}
