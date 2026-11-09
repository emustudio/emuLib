/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.application.ui.debugger;

import net.emustudio.emulib.plugin.cpu.CPU;
import net.emustudio.emulib.plugin.cpu.Disassembler;
import net.emustudio.emulib.plugin.cpu.InvalidInstructionException;

import java.util.Objects;
import java.util.function.IntFunction;

/**
 * Column in the debugger table. CPUs provide custom columns with {@link CPU#getDebuggerColumns()}.
 *
 * @param <T> Java type of this column
 */
public interface DebuggerColumn<T> {

    /**
     * @return Java type of this column
     */
    Class<T> getClassType();

    /**
     * @return title of this column
     */
    String getTitle();

    /**
     * Gets a value from the column cell.
     *
     * @param location Cell index (usually memory address). It is not a "row number" in the debugger table.
     * @return cell value
     */
    T getValue(int location);

    /**
     * Determines whether cells of this column are editable by user. Columns are read-only by default.
     *
     * @return true if the column is editable, false otherwise
     */
    default boolean isEditable() {
        return false;
    }

    /**
     * Set a value to a column cell of an editable column.
     *
     * @param location Cell index (usually memory address). It is not a "row number" in the debugger table.
     * @param value    new value assigned to the cell
     * @throws IllegalArgumentException if the value cannot be assigned
     */
    default void setValue(int location, Object value) {
    }

    /**
     * @return default width of the column in pixels or -1 if it is not known
     */
    default int getDefaultWidth() {
        return -1;
    }

    /**
     * Creates read-only text column.
     *
     * @param title  column title
     * @param values cell value by location
     * @return new column
     */
    static DebuggerColumn<String> text(String title, IntFunction<String> values) {
        Objects.requireNonNull(title);
        Objects.requireNonNull(values);
        return new DebuggerColumn<>() {
            @Override
            public Class<String> getClassType() {
                return String.class;
            }

            @Override
            public String getTitle() {
                return title;
            }

            @Override
            public String getValue(int location) {
                return values.apply(location);
            }
        };
    }

    /**
     * Creates "address" column.
     *
     * @param format address format for {@link String#format(String, Object...)}, e.g. {@code "%04X"}
     * @return new column
     */
    static DebuggerColumn<String> address(String format) {
        Objects.requireNonNull(format);
        return text("address", location -> String.format(format, location));
    }

    /**
     * Creates "instruction" column showing instruction mnemonics.
     *
     * @param disassembler the disassembler
     * @return new column
     */
    static DebuggerColumn<String> mnemo(Disassembler disassembler) {
        Objects.requireNonNull(disassembler);
        return text("instruction", location -> {
            try {
                return disassembler.disassemble(location).mnemo;
            } catch (InvalidInstructionException e) {
                return "[invalid]";
            } catch (IndexOutOfBoundsException e) {
                return "[incomplete]";
            }
        });
    }

    /**
     * Creates "opcode" column showing formatted binary representation of instructions.
     *
     * @param disassembler the disassembler
     * @return new column
     */
    static DebuggerColumn<String> opcode(Disassembler disassembler) {
        Objects.requireNonNull(disassembler);
        return text("opcode", location -> {
            try {
                return disassembler.disassemble(location).opCode;
            } catch (InvalidInstructionException | IndexOutOfBoundsException e) {
                return "";
            }
        });
    }
}
