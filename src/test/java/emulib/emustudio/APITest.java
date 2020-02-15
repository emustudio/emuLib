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
package emulib.emustudio;

import emulib.emustudio.debugtable.DebugTable;
import emulib.runtime.exceptions.InvalidPasswordException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
    public void tearDown() throws InvalidPasswordException {
        apiInstance.clearAll(password);
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
        apiInstance.getDebugTable().refresh();
        API.getInstance().setDebugTable(null, password);
        verify(debug);
    }

    @Test(expected = InvalidPasswordException.class)
    public void testInvalidPassword() throws InvalidPasswordException {
        API.testPassword(password + "definitely invalid");
    }

    @Test(expected = InvalidPasswordException.class)
    public void testNullPassword() throws InvalidPasswordException {
        API.testPassword((String)null);
    }

    @Test
    public void testHashCodePassword() {
        assertTrue(API.testPassword((long)password.hashCode()));
    }

    @Test
    public void testNullHashCodePassword() {
        assertFalse(API.testPassword((Long)null));
    }

    @Test
    public void testInvalidHashCodePassword() {
        assertFalse(API.testPassword((long)password.hashCode() + 1));
    }


}
