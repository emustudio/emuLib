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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class AddressColumnTest {

    @Test(expected = NullPointerException.class)
    public void testNullAddressFormatThrows() throws Exception {
        new AddressColumn(null);
    }

    @Test
    public void testCustomFormatWorks() throws Exception {
        AddressColumn addressColumn = new AddressColumn("%d");

        assertEquals("53", addressColumn.getValue(53));
    }

    @Test
    public void testDefaultFormatWorks() throws Exception {
        AddressColumn addressColumn = new AddressColumn();

        assertEquals("00FF", addressColumn.getValue(0xFF));
    }

    @Test
    public void testClassTypeIsString() throws Exception {
        AddressColumn addressColumn = new AddressColumn();

        assertEquals(String.class, addressColumn.getClassType());
    }

    @Test
    public void testAddressColumnTitleIsCorrect() throws Exception {
        AddressColumn addressColumn = new AddressColumn();

        assertEquals("address", addressColumn.getTitle());
    }

    @Test
    public void testColumnIsNotEditable() throws Exception {
        AddressColumn column = new AddressColumn();

        assertFalse(column.isEditable());
    }

    @Test
    public void testDefaultWidthIsNotPredefined() throws Exception {
        assertEquals(-1, new AddressColumn().getDefaultWidth());
    }

    @Test
    public void testSetDebugValueJustForIncreasingCoverage() throws Exception {
        new AddressColumn().setValue(-1, null);
    }

}
