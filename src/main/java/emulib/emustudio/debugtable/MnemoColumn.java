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

package emulib.emustudio.debugtable;

import emulib.plugins.cpu.AbstractDebugColumn;
import emulib.plugins.cpu.DisassembledInstruction;
import emulib.plugins.cpu.Disassembler;
import emulib.runtime.exceptions.InvalidInstructionException;

import java.util.Objects;

/**
 * This class represents "mnemo" column in the debug table.
 *
 * The column displays mnemonic (textual) representations of the instruction at specific location..
 */
public class MnemoColumn extends AbstractDebugColumn {
    private final Disassembler disassembler;

    public MnemoColumn(Disassembler disassembler) {
        super("mnemonics", String.class, false);
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
     * Get mnemonic form of an instruction at specific location.
     *
     * @param location  memory address (not row in debug table)
     * @return String containing mnemonic form of an instruction at specific location
     */
    @Override
    public Object getDebugValue(int location) {
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
