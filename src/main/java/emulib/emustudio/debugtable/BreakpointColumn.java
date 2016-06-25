/*
 * KISS, YAGNI, DRY
 *
 * (c) Copyright 2006-2016, Peter Jakubčo
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package emulib.emustudio.debugtable;

import emulib.plugins.cpu.AbstractDebugColumn;
import emulib.plugins.cpu.CPU;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class BreakpointColumn extends AbstractDebugColumn {
    private final static Logger LOGGER = LoggerFactory.getLogger(BreakpointColumn.class);

    private final CPU cpu;

    public BreakpointColumn(CPU cpu) {
        super("bp", java.lang.Boolean.class, true);
        this.cpu = Objects.requireNonNull(cpu);
    }

    /**
     * Set or unset breakpoint to specific location.
     *
     * @param location memory address (not row in debug table)
     * @param value  boolean value (set/unset breakpoint)
     */
    @Override
    public void setDebugValue(int location, Object value) {
        if (cpu.isBreakpointSupported()) {
            boolean shouldSet = Boolean.valueOf(value.toString());
            try {
                if (shouldSet) {
                    cpu.setBreakpoint(location);
                } else {
                    cpu.unsetBreakpoint(location);
                }
            } catch (IndexOutOfBoundsException e) {
                LOGGER.error(
                        "Could not " + (shouldSet ? "set" : "unset")  + " breakpoint to address {}",
                        String.format("%04xh", location), e
                );
            }
        }
    }

    /**
     * Determine if a breakpoint is set at specific location.
     *
     * @param location  memory address (not row in debug table)
     * @return boolean value (true/false) if breakpoint is set/unset at the location
     */
    @Override
    public Object getDebugValue(int location) {
        if (!cpu.isBreakpointSupported()) {
            return false;
        }
        try {
            return cpu.isBreakpointSet(location);
        } catch(IndexOutOfBoundsException e) {
            return false;
        }
    }

    @Override
    public boolean isEditable() {
        return cpu.isBreakpointSupported();
    }


    @Override
    public int getDefaultWidth() {
        return 20;
    }

}
