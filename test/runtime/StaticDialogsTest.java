/*
 * StaticDialogsTest.java
 *
 * (c) Copyright 2010, P. Jakubčo <pjakubco@gmail.com>
 *
 * KISS, YAGNI
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

package runtime;

import emuLib8.runtime.StaticDialogs;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author vbmacher
 */
public class StaticDialogsTest {

    public StaticDialogsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of showErrorMessage method, of class StaticDialogs.
     */
    @Test
    public void testShowErrorMessage_String() {
        System.out.println("showErrorMessage");
        String message = "Message";
        StaticDialogs.showErrorMessage(message);
    }

    /**
     * Test of showErrorMessage method, of class StaticDialogs.
     */
    @Test
    public void testShowErrorMessage_String_String() {
        System.out.println("showErrorMessage");
        String message = "Message";
        String title = "Title";
        StaticDialogs.showErrorMessage(message, title);
    }

    /**
     * Test of showMessage method, of class StaticDialogs.
     */
    @Test
    public void testShowMessage_String() {
        System.out.println("showMessage");
        String message = "Message";
        StaticDialogs.showMessage(message);
    }

    /**
     * Test of showMessage method, of class StaticDialogs.
     */
    @Test
    public void testShowMessage_String_String() {
        System.out.println("showMessage");
        String message = "Message";
        String title = "Title";
        StaticDialogs.showMessage(message, title);
    }

    /**
     * Test of inputIntValue method, of class StaticDialogs.
     */
    @Test
    public void testInputIntValue_String() {
        System.out.println("inputIntValue");
        String message = "Insert 1";
        Integer expResult = 1;
        Integer result = StaticDialogs.inputIntValue(message);
        assertEquals(expResult, result);
    }

    /**
     * Test of inputIntValue method, of class StaticDialogs.
     */
    @Test
    public void testInputIntValue_3args() {
        System.out.println("inputIntValue");
        String message = "Initial should be 0. Insert 1";
        String title = "Title";
        int initial = 0;
        Integer expResult = 1;
        Integer result = StaticDialogs.inputIntValue(message, title, initial);
        assertEquals(expResult, result);
    }

    /**
     * Test of inputStringValue method, of class StaticDialogs.
     */
    @Test
    public void testInputStringValue_String() {
        System.out.println("inputStringValue");
        String message = "Insert 'hello'";
        String expResult = "hello";
        String result = StaticDialogs.inputStringValue(message);
        assertEquals(expResult, result);
    }

    /**
     * Test of inputStringValue method, of class StaticDialogs.
     */
    @Test
    public void testInputStringValue_3args() {
        System.out.println("inputStringValue");
        String message = "Initial should be set to 'h'. Insert 'hello'";
        String title = "Title";
        String initial = "h";
        String expResult = "hello";
        String result = StaticDialogs.inputStringValue(message, title, initial);
        assertEquals(expResult, result);
    }

    /**
     * Test of inputDoubleValue method, of class StaticDialogs.
     */
    @Test
    public void testInputDoubleValue_String() {
        System.out.println("inputDoubleValue");
        String message = "Insert 3.5";
        Double expResult = 3.5;
        Double result = StaticDialogs.inputDoubleValue(message);
        assertEquals(expResult, result);
    }

    /**
     * Test of inputDoubleValue method, of class StaticDialogs.
     */
    @Test
    public void testInputDoubleValue_3args() {
        System.out.println("inputDoubleValue");
        String message = "Initial should be 2.8. Insert 3.5";
        String title = "Title";
        double initial = 2.8;
        Double expResult = 3.5;
        Double result = StaticDialogs.inputDoubleValue(message, title, initial);
        assertEquals(expResult, result);
    }

    /**
     * Test of confirmMessage method, of class StaticDialogs.
     */
    @Test
    public void testConfirmMessage_String() {
        System.out.println("confirmMessage");
        String message = "Confirm it!! Click YES";
        int expResult = StaticDialogs.YES_OPTION;
        int result = StaticDialogs.confirmMessage(message);
        assertEquals(expResult, result);
    }

    /**
     * Test of confirmMessage method, of class StaticDialogs.
     */
    @Test
    public void testConfirmMessage_String_String() {
        System.out.println("confirmMessage");
        String message = "Click NO";
        String title = "Title";
        int expResult = StaticDialogs.NO_OPTION;
        int result = StaticDialogs.confirmMessage(message, title);
        assertEquals(expResult, result);
    }

}