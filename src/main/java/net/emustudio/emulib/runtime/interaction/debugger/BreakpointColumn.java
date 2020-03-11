/*
 * This file is part of emuLib.
 *
 * Copyright (C) 2006-2020  Peter Jakubčo
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

import net.emustudio.emulib.plugins.cpu.CPU;

import java.util.Objects;

public class BreakpointColumn implements DebuggerColumn<Boolean> {
    private final CPU cpu;

    public BreakpointColumn(CPU cpu) {
        this.cpu = Objects.requireNonNull(cpu);
    }

    @Override
    public Class<Boolean> getClassType() {
        return Boolean.class;
    }

    @Override
    public String getTitle() {
        return "bp";
    }

    @Override
    public boolean isEditable() {
        return cpu.isBreakpointSupported();
    }

    /**
     * Determine if a breakpoint is set at specific location.
     *
     * @param location memory address (not row in debug table)
     * @return boolean value (true/false) if breakpoint is set/unset at the location
     */
    @Override
    public Boolean getValue(int location) {
        if (cpu.isBreakpointSupported()) {
            try {
                return cpu.isBreakpointSet(location);
            } catch (IndexOutOfBoundsException e) {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Set or unset breakpoint to specific location.
     *
     * @param location        memory address (not row in debug table)
     * @param shouldSetObject boolean value (set/unset breakpoint)
     */
    @Override
    public void setValue(int location, Object shouldSetObject) throws CannotSetDebuggerValueException {
        Boolean shouldSet = (Boolean) shouldSetObject;

        if (isEditable()) {
            try {
                if (shouldSet) {
                    cpu.setBreakpoint(location);
                } else {
                    cpu.unsetBreakpoint(location);
                }
            } catch (IndexOutOfBoundsException e) {
                String msg = "Could not " + (shouldSet ? "set" : "unset") + " breakpoint to location " + String.format("%04xh", location);
                throw new CannotSetDebuggerValueException(msg, e);
            }
        }
    }

    @Override
    public int getDefaultWidth() {
        return 20;
    }
}
