/*
 * APITest.java
 *
 * KISS, YAGNI, DRY
 * 
 * (c) Copyright 2012, Peter Jakubčo
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
package emulib.emustudio;

import emulib.runtime.IDebugTable;
import junit.framework.TestCase;

/**
 *
 * @author vbmacher
 */
public class APITest extends TestCase {
    private final static String password = "password";
    private static boolean passwordAssigned = false;
    
    public class MockDebugTable implements IDebugTable {

        @Override
        public void updateDebugTable() {
            throw new IllegalStateException();
        }
        
    }
    
    public APITest(String testName) {
        super(testName);
    }

    /**
     * Test of getInstance method, of class API.
     */
    public void testGetInstance() {
        API result = API.getInstance();
        assertNotNull(result);
        assertEquals(result, API.getInstance());
    }
    
    public static String getEmuStudioPassword() {
        return password;
    }

    public synchronized static void assignEmuStudioPassword() {
        if (!passwordAssigned) {
            assertTrue(API.assignPassword(password));
            passwordAssigned = true;
            assertFalse(API.assignPassword("dsfsf"));
        } else {
            assertFalse(API.assignPassword("dsfsf"));
        }
    }
    
    /**
     * Test of emuStudio password assignment
     */
    public void testAssignPassword() {
        assignEmuStudioPassword();
    }
    

    /**
     * Test of setDebugTableInterfaceObject method, of class API.
     */
    public void testSetDebugTableInterfaceObject() {
        IDebugTable debug = new MockDebugTable();
        assignEmuStudioPassword(); 
        API instance = API.getInstance();
        instance.setDebugTableInterfaceObject(debug, password);
        try {
            instance.updateDebugTable();
        } catch (IllegalStateException e) {
            // clean
            API.getInstance().setDebugTableInterfaceObject(null, password);
            return;
        }
        fail("updateDebugTable() wasn't called!");
    }

}
