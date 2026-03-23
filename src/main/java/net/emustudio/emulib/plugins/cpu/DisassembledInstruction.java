/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.cpu;

import net.jcip.annotations.Immutable;

/**
 * This class represents disassembled instruction. It is used by disassembler.
 * <p>
 * It is immutable.
 */
@Immutable
public class DisassembledInstruction {
    /**
     * Instruction address
     */
    public final int address;

    /**
     * Instruction mnemonic and operand code in textual representation.
     */
    public final String mnemo;

    /**
     * Instruction opcode in hex string (as sequence of bytes)
     */
    public final String opCode;

    /**
     * Creates a new instance of DisassembledInstruction
     *
     * @param address The address of the instruction in memory
     * @param mnemo   The mnemonic form of the instruction (textual representation)
     * @param opCode  Operating code in textual representation
     */
    public DisassembledInstruction(int address, String mnemo, String opCode) {
        this.mnemo = mnemo;
        this.opCode = opCode;
        this.address = address;
    }

    @Override
    public String toString() {
        return address + " | " + mnemo + " | " + opCode;
    }
}
