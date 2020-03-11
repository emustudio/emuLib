// SPDX-License-Identifier: GPL-3.0-or-later
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
