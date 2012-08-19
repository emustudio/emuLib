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

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author vbmacher
 */
public class APITest {
    private final static String password = "password";
    private static boolean passwordAssigned = false;
    
    public class DebugTableStub implements DebugTable {

        @Override
        public void refresh() {
            throw new IllegalStateException();
        }
        
    }
    
    /**
     * Test of getInstance method, of class API.
     */
    @Test
    public void testGetInstance() {
        API result = API.getInstance();
        Assert.assertNotNull(result);
        Assert.assertEquals(result, API.getInstance());
    }
    
    public static String getEmuStudioPassword() {
        return password;
    }

    public synchronized static void assignEmuStudioPassword() {
        if (!passwordAssigned) {
            Assert.assertTrue(API.assignPassword(password));
            passwordAssigned = true;
            Assert.assertFalse(API.assignPassword("dsfsf"));
        } else {
            Assert.assertFalse(API.assignPassword("dsfsf"));
        }
    }
    
    /**
     * Test of emuStudio password assignment
     */
    @Test
    public void testAssignPassword() {
        assignEmuStudioPassword();
    }
    

    /**
     * Test of setDebugTable method, of class API.
     */
    @Test
    public void testSetDebugTableInterfaceObject() {
        DebugTable debug = new DebugTableStub();
        assignEmuStudioPassword(); 
        API instance = API.getInstance();
        instance.setDebugTable(debug, password);
        try {
            instance.refreshDebugTable();
        } catch (IllegalStateException e) {
            // clean
            API.getInstance().setDebugTable(null, password);
            return;
        }
        Assert.fail("updateDebugTable() wasn't called!");
    }

}
