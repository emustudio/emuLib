/*
 * Run-time library for emuStudio and plug-ins.
 *
 *     Copyright (C) 2006-2020  Peter Jakubčo
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.emustudio.emulib.plugins.cpu;

import net.emustudio.emulib.runtime.exceptions.InvalidInstructionException;

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
     */
    DisassembledInstruction disassemble(int memoryPosition) throws InvalidInstructionException;

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
