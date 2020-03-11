// SPDX-License-Identifier: GPL-3.0-or-later
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
