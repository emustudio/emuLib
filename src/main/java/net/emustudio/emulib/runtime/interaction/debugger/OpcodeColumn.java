// SPDX-License-Identifier: GPL-3.0-or-later
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
