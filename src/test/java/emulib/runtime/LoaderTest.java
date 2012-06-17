/*
 * LoaderTest.java
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

import emulib.plugins.IPlugin;
import emulib.plugins.ISettingsHandler;
import emulib.plugins.cpu.ICPU;
import emulib.plugins.cpu.ICPU.ICPUListener;
import emulib.plugins.cpu.ICPU.RunState;
import emulib.plugins.cpu.IDisassembler;
import java.util.EventObject;
import javax.swing.JPanel;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author vbmacher
 */
public class LoaderTest extends TestCase {
    
    private class MockCPUListener implements ICPUListener {
        @Override
        public void runChanged(EventObject evt, RunState runState) {}
        @Override
        public void stateUpdated(EventObject evt) {}
    }
    
    private abstract class MockSuperCPU implements ICPU { }
    private class MockCPUImpl extends MockSuperCPU {
        @Override
        public void addCPUListener(ICPUListener listener) {}
        @Override
        public void removeCPUListener(ICPUListener listener) {}
        @Override
        public void step() {}
        @Override
        public void execute() {}
        @Override
        public void pause() {}
        @Override
        public void stop() {}
        @Override
        public JPanel getStatusGUI() { return null; }
        @Override
        public boolean isBreakpointSupported() { return false; }
        @Override
        public void setBreakpoint(int pos, boolean set) {}
        @Override
        public boolean getBreakpoint(int pos) { return false; }
        @Override
        public void reset(int startAddress) {}
        @Override
        public int getInstrPosition() { return 0; }
        @Override
        public boolean setInstrPosition(int pos) { return false; }
        @Override
        public IDisassembler getDisassembler() { return null; }
        @Override
        public void reset() {}
        @Override
        public String getTitle() { return ""; }
        @Override
        public String getCopyright() { return ""; }
        @Override
        public String getDescription() { return ""; }
        @Override
        public String getVersion() { return ""; }
        @Override
        public boolean initialize(ISettingsHandler sHandler) { return false; }
        @Override
        public void destroy() {}
        @Override
        public void showSettings() {}
        @Override
        public boolean isShowSettingsSupported() { return false; }
    }
    

    public LoaderTest(String testName) {
        super(testName);
    }

   /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( LoaderTest.class );
    }

    /**
     * Test of getInstance method, of class Loader.
     */
    public void testGetInstance() {
        ContextTest.assignEmuStudioPassword();
        PluginLoader expResult = PluginLoader.getInstance("password");
        PluginLoader result = PluginLoader.getInstance("password");
        assertEquals(expResult, result);
    }

    /**
     * Test of loadJAR method, of class Loader.
     */
    public void testLoadJAR() {
        String filename = System.getProperty("user.dir") + "/src/test/resources/8080-cpu.jar";
        ContextTest.assignEmuStudioPassword();
        PluginLoader instance = PluginLoader.getInstance("password");
        Class<IPlugin> result = instance.loadPlugin(filename);
        assertNotNull(result);
    }
    
    /**
     * Test crucial method for finding plug-ins' main interface.
     */
    public void testDoesImplement() {
        ContextTest.assignEmuStudioPassword();
        PluginLoader instance = PluginLoader.getInstance("password");
        
        // test for nested interface
        assertFalse(instance.doesImplement(MockCPUListener.class, IPlugin.class));
        
        // test for inherited interface
        assertTrue(instance.doesImplement(MockCPUImpl.class, IPlugin.class));
    }

}
