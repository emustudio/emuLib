// SPDX-License-Identifier: GPL-3.0-or-later
package net.emustudio.emulib.runtime.interaction.debugger;

import java.util.Objects;

public class AddressColumn implements DebuggerColumn<String> {
    private final String addressFormat;

    /**
     * Create new instance of the address column.
     *
     * Address format is <code>"%04X"</code>.
     */
    public AddressColumn() {
        this("%04X");
    }

    /**
     * Create new instance of the address column.
     *
     * @param addressFormat Address format used for calling <code>String.format()</code>
     */
    public AddressColumn(String addressFormat) {
        this.addressFormat = Objects.requireNonNull(addressFormat);
    }

    @Override
    public Class<String> getClassType() {
        return String.class;
    }

    @Override
    public String getTitle() {
        return "address";
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
     * Return formatted address into hexadecimal digit, aligned to 4 digits.
     *
     * @param location  memory address (not row in debug table)
     * @return String value with formatted address
     */
    @Override
    public String getValue(int location) {
        return String.format(addressFormat, location);
    }

    @Override
    public int getDefaultWidth() {
        return -1;
    }

}
