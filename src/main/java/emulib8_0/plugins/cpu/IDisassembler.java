/*
 * IDisassembler.java
 *
 * KISS, YAGNI
 *
 * Copyright (C) 2011 Peter Jakubčo <pjakubco at gmail.com>
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

package emulib8_0.plugins.cpu;

/**
 *
 * @author vbmacher
 */
public interface IDisassembler {

    /**
     * Convert memory location at current instruction into row index in
     * debug table (in emuStudio).
     *
     * @return row index in debug row
     */
//    public int locationToRow();

    /**
     * Convert memory location into row index in debug table (in emuStudio).
     *
     * @param location Memory address (location)
     * @return row index in debug row
     */
  //  public int locationToRow(int location);

    /**
     * Determines whether a debug row points at current instruction
     *
     * @param debugRow row index in debug table
     * @return true is the debug row points at current instruction, false
     * otherwise
     */
 //   public boolean isRowCurrent(int debugRow);

    /**
     * Dissassemble one instruction at specific location.
     *
     * @param memLocation
     *   Memory address where to begin disassembling.
     * @return
     *   Object that represents the disassembled instruction.
     */
    public CPUInstruction disassemble(int memLocation);

    /**
     * Returns an address in the memory of the next instruction, that is
     * followed by specified location.
     *
     * @param memLocation The memory location of the instruction
     * @return Memory location of next instruction
     * @throws IndexOutOfBoundsException when memory location exceeds the bounds
     */
    public int getNextInstructionLocation(int memLocation)
            throws IndexOutOfBoundsException;

    /**
     * Returns memory address of previous instruction.
     *
     * This method relies hlighly upon von-Neuman sequential model of
     * computation.
     *
     * @param memLocation The memory location of current instruction
     * @return memory location of previous instruction. If no instruction can be
     * found (the location is invalid), negative value is returned.
     */
    public int getPreviousInstructionLocation(int memLocation);
}
