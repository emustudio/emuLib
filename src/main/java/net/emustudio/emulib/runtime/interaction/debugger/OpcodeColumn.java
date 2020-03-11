/*
 * Run-time library for emuStudio and plugins.
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
package net.emustudio.emulib.runtime.interaction.debugger;

import net.emustudio.emulib.plugins.cpu.Disassembler;
import net.emustudio.emulib.plugins.cpu.InvalidInstructionException;

import java.util.Objects;

/**
 * This class represents "opcode" column in the debug table.
 *
 * The opcode means operating code - the formatted binary representation of the instruction.
 */
public class OpcodeColumn implements DebuggerColumn<String> {
    private final Disassembler disassembler;

    public OpcodeColumn(Disassembler disassembler) {
        this.disassembler = Objects.requireNonNull(disassembler);
    }

    @Override
    public Class<String> getClassType() {
        return String.class;
    }

    @Override
    public String getTitle() {
        return "opcode";
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    /**
     * Has no effect.
     *
     * @param location memory address (not row in debug table)
     * @param value  new value of the cell
     */
    @Override
    public void setValue(int location, Object value) {

    }

    /**
     * Get opcode for instruction at specific location.
     *
     * @param location  memory address (not row in debug table)
     * @return Opcode string of an instruction at specific location
     */
    @Override
    public String getValue(int location) {
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
