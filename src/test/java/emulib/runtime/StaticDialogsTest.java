/*
 * KISS, YAGNI, DRY
 *
 * (c) Copyright 2006-2016, Peter Jakubčo
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package emulib.runtime;

import emulib.emustudio.APITest;
import org.junit.Before;
import org.junit.Test;

import java.net.MalformedURLException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class StaticDialogsTest {

    @Before
    public void setUp() throws MalformedURLException {
        APITest.assignEmuStudioPassword();
    }

    @Test
    public void testSetGetGuiSupported() throws InvalidPasswordException {
        StaticDialogs.setGUISupported(true, APITest.getEmuStudioPassword());
        assertTrue(StaticDialogs.isGUISupported());
        StaticDialogs.setGUISupported(false, APITest.getEmuStudioPassword());
        assertFalse(StaticDialogs.isGUISupported());
    }

    @Test
    public void testInputReturnsNullWhenGUIisNotSupported() throws InvalidPasswordException {
        StaticDialogs.setGUISupported(false, APITest.getEmuStudioPassword());

        assertNull(StaticDialogs.inputDoubleValue("A"));
        assertNull(StaticDialogs.inputDoubleValue("A", "B", 0.4));
        assertNull(StaticDialogs.inputIntValue("A"));
        assertNull(StaticDialogs.inputIntValue("A", "B", 3));
        assertNull(StaticDialogs.inputStringValue("A"));
        assertNull(StaticDialogs.inputStringValue("A", "B", "C"));
    }

    @Test
    public void testShowDialogsDontShowNothingWhenGUIisNotSupported() throws InvalidPasswordException {
        StaticDialogs.setGUISupported(false, APITest.getEmuStudioPassword());

        StaticDialogs.confirmMessage("A");
        StaticDialogs.confirmMessage("A", "B");
        StaticDialogs.showErrorMessage("A");
        StaticDialogs.showErrorMessage("A", "B");
        StaticDialogs.showMessage("A");
        StaticDialogs.showMessage("A", "B");
    }


}
