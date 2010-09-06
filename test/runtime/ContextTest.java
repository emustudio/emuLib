/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package runtime;

import plugins.cpu.ICPUContext;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import plugins.device.IDeviceContext;

/**
 *
 * @author vbmacher
 */
public class ContextTest {

    public interface ITestContext extends ICPUContext {
        public void testMethod();
    }

    public class TestContext implements ITestContext {

        @Override
        public void testMethod() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

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

    }

    public ContextTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testInterfaces() {
        TestContext context = new TestContext();
        
        Class<?>[] intf = context.getClass().getInterfaces();
        for (int i = 0; i < intf.length; i++) {
            System.out.print(intf[i].toString());
            System.out.println(" instanceof ICPUContext: " + (context instanceof ICPUContext));
            System.out.println("(" + intf[i].getSimpleName()  +  ")");
        }
    }

}