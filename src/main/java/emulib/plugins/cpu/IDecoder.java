/*
 * IDecoder.java
 *
 * (c) Copyright 2012, M. Sulír
 *
 * KISS, YAGNI, DRY
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
 * An instruction decoder interface.
 * @author Matúš Sulír
 */
public interface IDecoder {
    /**
     * Decodes an instruction.
     * @param memoryPosition the address of the start of the instruction
     * @return the decoded instruction object
     * @throws InvalidInstructionException when decoding is not successful
     */
    DecodedInstruction decode(int memoryPosition) throws InvalidInstructionException;
}
