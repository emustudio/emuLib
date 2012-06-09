/*
 * ContextTest.java
 * 
 * KISS, YAGNI, DRY
 * 
 * (c) Copyright 2010-2012, Peter Jakubčo
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

import emulib.plugins.cpu.ICPUContext;
import emulib.plugins.device.IDeviceContext;
import emulib.runtime.interfaces.IConnections;
import junit.framework.TestCase;

/**
 *
 * @author vbmacher
 */
public class ContextTest extends TestCase {
    private final static String password = "password";
    private static boolean passwordAssigned = false;

    public class MockContext implements C4664566E71E3C14D1732E34E2F66E8E31EE6951E {

        @Override
        public String getID() {
            return null;
        }

        @Override
        public boolean isInterruptSupported() {
            return false;
        }

        @Override
        public void setInterrupt(IDeviceContext device, int mask) {
        }

        @Override
        public void clearInterrupt(IDeviceContext device, int mask) {
        }

        @Override
        public void testMethod() {
        }

    }
    
    public void testPasswordAssign() {
        if (!passwordAssigned) {
            assertTrue(Context.assignPassword(password));
            passwordAssigned = true;
        }
        assertFalse(Context.assignPassword("dsfsf"));
    }
    
    /**
     * Test if context is singleton
     */
    public void testContextSingleton() {
        Context context1 = Context.getInstance();
        assertNotNull(context1);
        assertEquals(context1, Context.getInstance());
    }

    /**
     * Test registration of context
     */
    public void testContextRegistration() throws AlreadyRegisteredException, InvalidImplementationException, 
            InvalidHashException {
        MockContext context = new MockContext();
        Context cInstance = Context.getInstance();
        testPasswordAssign();
        
        assertTrue(cInstance.assignComputer(password, new IConnections() {

            @Override
            public int getPluginType(long pluginID) {
                return TYPE_CPU;
            }

            @Override
            public boolean isConnected(long pluginID, long toPluginID) {
                return true;
            }
        }));
        assertTrue(cInstance.register(0, context, C4664566E71E3C14D1732E34E2F66E8E31EE6951E.class));
        ICPUContext getContext = cInstance.getCPUContext(0, C4664566E71E3C14D1732E34E2F66E8E31EE6951E.class);
        assertEquals(context, getContext);
        cInstance.unregister(0, C4664566E71E3C14D1732E34E2F66E8E31EE6951E.class);
    }

}
