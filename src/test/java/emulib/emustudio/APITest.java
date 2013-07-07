/*
 * APITest.java
 *
 * KISS, YAGNI, DRY
 *
 * (c) Copyright 2012-2013, Peter Jakubčo
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

import emulib.runtime.InvalidPasswordException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.easymock.EasyMock.*;
import org.junit.After;

/**
 *
 * @author vbmacher
 */
public class APITest {
    private final static String password = "password";
    private static boolean passwordAssigned = false;
    private API apiInstance;

    @Before
    public void setUp() {
        assignEmuStudioPassword();
        apiInstance = API.getInstance();
        Assert.assertNotNull(apiInstance);
    }

    @After
    public void tearDown() {
        apiInstance = null;
    }

    @Test
    public void testGetInstance() {
        Assert.assertEquals(apiInstance, API.getInstance());
    }

    public static String getEmuStudioPassword() {
        return password;
    }

    public static void assignEmuStudioPassword() {
        if (!passwordAssigned) {
            Assert.assertTrue(API.assignPassword(password));
            passwordAssigned = true;
            Assert.assertFalse(API.assignPassword("dsfsf"));
        } else {
            Assert.assertFalse(API.assignPassword("dsfsf"));
        }
    }

    @Test
    public void testRefreshDebugTable() throws InvalidPasswordException {
        DebugTable debug = createNiceMock(DebugTable.class);
        debug.refresh();
        expectLastCall().once();
        replay(debug);

        apiInstance.setDebugTable(debug, password);
        apiInstance.refreshDebugTable();
        API.getInstance().setDebugTable(null, password);
        verify(debug);
    }

    public void testRefreshUnsetDebugTable() {
        apiInstance.refreshDebugTable();

    }

    @Test(expected = InvalidPasswordException.class)
    public void testInvalidPassword() throws InvalidPasswordException {
        API.testPassword(password + "definitely invalid");
    }

    @Test(expected = InvalidPasswordException.class)
    public void testNullPassword() throws InvalidPasswordException {
        API.testPassword(null);
    }
}
