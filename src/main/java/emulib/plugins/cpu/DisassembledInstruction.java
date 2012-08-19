/*
 * DisassembledInstruction.java
 * 
 * KISS, YAGNI, DRY
 *
 * Copyright (C) 2011-2012, Peter Jakubčo
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package emulib.plugins.cpu;

/**
 * This class represents disassembled instruction. It is used by
 * disassembler.
 *
 * It is implemented as read/write container that stores address, mnemo
 * and opcode of the instruction.
 *
 */
public class DisassembledInstruction {
    private int address;
    private String mnemo;
    private String operCode;

    /**
     * Creates a new instance of DisassembledInstruction
     * @param address The address of the instruction in memory
     * @param mnemo   The mnemonic form of the instruction (textual representation)
     * @param opCode  Operating code in textual representation
     */
    public DisassembledInstruction(int address, String mnemo, String opCode) {
        this.mnemo = mnemo;
        this.operCode = opCode;
        this.address = address;
    }

    /**
     * Returns mnemonic representation of the instruction.
     * @return a string representing mnemonics form of the instruction
     */
    public String getMnemo() {
        return this.mnemo;
    }

    /**
     * Returns operating code of the instruction.
     *
     * @return a string that represents the operating code of the instruction
     */
    public String getOpCode() {
        return this.operCode;
    }

    /**
     * Assigns new/modified instruction.
     *
     * @param mnemo new mnemonics representation of the instruction
     * @param opcode new operating code
     */
    public void setInstruction(String mnemo, String opcode) {
        this.mnemo = mnemo;
        this.operCode = opcode;
    }

    /**
     * Returns the address of the instruction.
     *
     * @return address of the instruction
     */
    public int getAddress() {
        return address;
    }

    /**
     * Set the address of the instruction to a new value.
     *
     * @param address new address of the instruction
     */
    public void setAddress(int address) {
        this.address = address;
    }
}
