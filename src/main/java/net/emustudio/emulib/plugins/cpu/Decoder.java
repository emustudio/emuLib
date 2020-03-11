// SPDX-License-Identifier: GPL-3.0-or-later
package net.emustudio.emulib.plugins.cpu;

/**
 * An instruction decoder interface.
 */
@SuppressWarnings("unused")
public interface Decoder {
    /**
     * Decodes an instruction.
     *
     * @param memoryPosition the address of the start of the instruction
     * @return the decoded instruction object
     * @throws InvalidInstructionException when decoding is not successful
     */
    DecodedInstruction decode(int memoryPosition) throws InvalidInstructionException;

}
