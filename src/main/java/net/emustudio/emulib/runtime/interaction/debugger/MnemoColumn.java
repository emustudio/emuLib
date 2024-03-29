/*
 * This file is part of emuLib.
 *
 * Copyright (C) 2006-2023  Peter Jakubčo
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
package net.emustudio.emulib.runtime.interaction.debugger;

import net.emustudio.emulib.plugins.cpu.DisassembledInstruction;
import net.emustudio.emulib.plugins.cpu.Disassembler;
import net.emustudio.emulib.plugins.cpu.InvalidInstructionException;

import java.util.Objects;

/**
 * This class represents "mnemo" column in the debug table.
 *
 * The column displays mnemonic (textual) representations of the instruction at specific location..
 */
public class MnemoColumn implements DebuggerColumn<String> {
    private final Disassembler disassembler;

    public MnemoColumn(Disassembler disassembler) {
        this.disassembler = Objects.requireNonNull(disassembler);
    }

    @Override
    public Class<String> getClassType() {
        return String.class;
    }

    @Override
    public String getTitle() {
        return "instruction";
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
     * Get instruction mnemonic.
     *
     * @param location  memory address (not row in debug table)
     * @return Mnemonic form of an instruction at specific location
     */
    @Override
    public String getValue(int location) {
        try {
            DisassembledInstruction instr = disassembler.disassemble(location);
            return instr.getMnemo();
        } catch (InvalidInstructionException e) {
            return "[invalid]";
        } catch(IndexOutOfBoundsException e) {
            return "[incomplete]";
        }
    }

    @Override
    public int getDefaultWidth() {
        return -1;
    }
}
