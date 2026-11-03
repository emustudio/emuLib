/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.cpu;

/**
 * An instruction disassembler interface.
 */
@SuppressWarnings("unused")
public interface Disassembler {
    /**
     * Capture an immutable decoder for one explicit inspection bank. The returned decoder must
     * never consult live memory or the selected bank. Empty means stable analysis is unsupported.
     * Callers pause execution and validate their memory/layout revision around capture and before
     * accepting derived results; this operation does not coordinate external memory writers.
     */
    default java.util.Optional<Disassembler> capture(int bank) { return java.util.Optional.empty(); }

    /** Optional structured flow information; providers must not infer it by parsing rendered mnemonics. */
    default java.util.Optional<InstructionFlow> getFlow(int address) { return java.util.Optional.empty(); }


    /**
     * Disassemble one instruction at specific location.
     *
     * @param memoryPosition Memory address where to begin disassembling.
     * @return Object that represents the disassembled instruction.
     * @throws InvalidInstructionException when instruction coul not be disassembled
     * @throws IndexOutOfBoundsException   when memory location exceeds the bounds
     */
    DisassembledInstruction disassemble(int memoryPosition) throws InvalidInstructionException, IndexOutOfBoundsException;

    /**
     * Returns an address in the memory of the next instruction, that is
     * followed by specified location.
     *
     * @param memoryPosition The memory location of the instruction
     * @return Memory position of next instruction
     * @throws IndexOutOfBoundsException when memory location exceeds the bounds
     */
    int getNextInstructionPosition(int memoryPosition) throws IndexOutOfBoundsException;

}
