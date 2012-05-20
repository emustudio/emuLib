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
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author vbmacher
 */
public class ContextTest extends TestCase {

    public ContextTest(String testName) {
        super(testName);
    }

    /**
     * 
     */
    public interface ITestContext extends ICPUContext {
        
        /**
         * 
         */
        public void testMethod();
    }

    /**
     * 
     */
    public class TestContext implements ITestContext {

        /**
         * 
         */
        @Override
        public void testMethod() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        /**
         * 
         * @return 
         */
        @Override
        public String getID() {
            return null;
        }

        /**
         * 
         * @return 
         */
        @Override
        public boolean isInterruptSupported() {
            return false;
        }

        /**
         * 
         * @param device
         * @param mask 
         */
        @Override
        public void setInterrupt(IDeviceContext device, int mask) {
        }

        /**
         * 
         * @param device
         * @param mask 
         */
        @Override
        public void clearInterrupt(IDeviceContext device, int mask) {
        }

    }

   /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( ContextTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        TestContext context = new TestContext();
        
        Class<?>[] intf = context.getClass().getInterfaces();
        for (int i = 0; i < intf.length; i++) {
            System.out.print(intf[i].toString());
            System.out.println(" instanceof ICPUContext: " + (context instanceof ICPUContext));
            System.out.println("(" + intf[i].getSimpleName()  +  ")");
        }
        assertTrue( true );
    }

}
