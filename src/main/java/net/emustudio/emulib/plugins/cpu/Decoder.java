/*
 * This file is part of emuLib.
 *
 * Copyright (C) 2012  Matúš Sulír
 * Copyright (C) 2012-2020 Peter Jakubčo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
