// SPDX-License-Identifier: GPL-3.0-or-later
package net.emustudio.emulib.plugins.cpu;

/**
 * An instruction disassembler interface.
 */
@SuppressWarnings("unused")
public interface Disassembler {

    /**
     * Dissassemble one instruction at specific location.
     *
     * @param memoryPosition
     *   Memory address where to begin disassembling.
     * @return Object that represents the disassembled instruction.
     * @throws InvalidInstructionException when instruction coul not be disassembled
     * @throws IndexOutOfBoundsException when memory location exceeds the bounds
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
