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
package net.emustudio.emulib.emustudio.debugtable;

import net.emustudio.emulib.plugins.cpu.AbstractDebugColumn;
import net.emustudio.emulib.plugins.cpu.Disassembler;
import net.emustudio.emulib.runtime.exceptions.InvalidInstructionException;

import java.util.Objects;

/**
 * This class represents "opcode" column in the debug table.
 *
 * The opcode means operating code - the formatted binary representation of the instruction.
 */
public class OpcodeColumn extends AbstractDebugColumn {
    private final Disassembler disassembler;

    public OpcodeColumn(Disassembler disassembler) {
        super("opcode", String.class, false);
        this.disassembler = Objects.requireNonNull(disassembler);
    }

    /**
     * Has no effect.
     *
     * @param location memory address (not row in debug table)
     * @param value  new value of the cell
     */
    @Override
    public void setDebugValue(int location, Object value) {
    }

    /**
     * Get opcode for an instruction at specific location.
     *
     * @param location  memory address (not row in debug table)
     * @return String containing the opcode of an instruction at specific location
     */
    @Override
    public Object getDebugValue(int location) {
        try {
            return disassembler.disassemble(location).getOpCode();
        } catch (InvalidInstructionException | IndexOutOfBoundsException e) {
            return "";
        }
    }

    @Override
    public int getDefaultWidth() {
        return -1;
    }

}
