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

import java.util.Objects;

/**
 * Address column for debug table.
 */
public class AddressColumn extends AbstractDebugColumn {
    private final String addressFormat;

    public AddressColumn() {
        this("%04X");
    }

    public AddressColumn(String addressFormat) {
        super("address", String.class, false);
        this.addressFormat = Objects.requireNonNull(addressFormat);
    }

    /**
     * Not used here.
     *
     * @param location memory address (not row in debug table)
     * @param value  new value of the cell
     */
    @Override
    public void setDebugValue(int location, Object value) {
    }

    /**
     * Return formatted address into hexadecimal digit, aligned to 4 digits.
     *
     * @param location  memory address (not row in debug table)
     * @return String value with formatted address
     */
    @Override
    public Object getDebugValue(int location) {
        return String.format(addressFormat, location);
    }

    @Override
    public int getDefaultWidth() {
        return -1;
    }

}
