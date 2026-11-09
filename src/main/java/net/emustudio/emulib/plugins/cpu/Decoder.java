/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.cpu;

/**
 * An instruction decoder interface.
 */
@SuppressWarnings("unused")
public interface Decoder {
    /**
     * Decodes an instruction.
     *
     * Callers must serialize access unless the implementation documents concurrent decoding.
     * A returned instruction is immutable even after the memory changes.
     *
     * @param address the logical address of the start of the instruction
     * @return the decoded instruction object
     * @throws InvalidInstructionException when decoding is not successful
     * @throws IndexOutOfBoundsException when required instruction cells are unavailable
     */
    DecodedInstruction decode(int address) throws InvalidInstructionException, IndexOutOfBoundsException;

}
