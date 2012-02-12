/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
