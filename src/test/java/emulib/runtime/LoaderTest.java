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

import emulib.annotations.PLUGIN_TYPE;
import emulib.annotations.PluginType;
import emulib.emustudio.APITest;
import emulib.emustudio.SettingsManager;
import emulib.plugins.Plugin;
import emulib.plugins.cpu.CPU;
import emulib.plugins.cpu.CPU.CPUListener;
import emulib.plugins.cpu.CPU.RunState;
import emulib.plugins.cpu.Disassembler;
import javax.swing.JPanel;
import junit.framework.TestCase;

public class LoaderTest extends TestCase {
    
    private class CPUListenerStub implements CPUListener {
        @Override
        public void runChanged(RunState runState) {}
        @Override
        public void stateUpdated() {}
    }
    
    private abstract class SuperCPUStub implements CPU { }
    
    @PluginType(title="CPU", description="", type=PLUGIN_TYPE.CPU,
            copyright="(c)")
    private class CPUImplStub extends SuperCPUStub {
        @Override
        public boolean addCPUListener(CPUListener listener) {
            return true;
        }
        @Override
        public boolean removeCPUListener(CPUListener listener) {
            return true;
        }
        @Override
        public void step() {}
        @Override
        public void execute() {}
        @Override
        public void pause() {}
        @Override
        public void stop() {}
        @Override
        public JPanel getStatusPanel() { return null; }
        @Override
        public boolean isBreakpointSupported() { return false; }
        @Override
        public void setBreakpoint(int pos) {}
        @Override
        public void unsetBreakpoint(int pos) {}
        @Override
        public boolean isBreakpointSet(int pos) { return false; }
        @Override
        public void reset(int startAddress) {}
        @Override
        public int getInstructionPosition() { return 0; }
        @Override
        public boolean setInstructionPosition(int pos) { return false; }
        @Override
        public Disassembler getDisassembler() { return null; }
        @Override
        public void reset() {}
        @Override
        public boolean initialize(SettingsManager sHandler) { return false; }
        @Override
        public void destroy() {}
        @Override
        public void showSettings() {}
        @Override
        public boolean isShowSettingsSupported() { return false; }
        @Override
        public String getVersion() { return ""; }
        @Override
        public String getTitle() {
            return CPUImplStub.class.getAnnotation(PluginType.class).title();
        }
    }

    public LoaderTest(String testName) {
        super(testName);
    }

    /**
     * Test of getInstance method, of class Loader.
     */
    public void testGetInstance() {
        APITest.assignEmuStudioPassword();
        PluginLoader expResult = PluginLoader.getInstance();
        PluginLoader result = PluginLoader.getInstance();
        assertEquals(expResult, result);
    }

    /**
     * Test of loadJAR method, of class Loader.
     */
    public void testLoadJAR() throws InvalidPasswordException, InvalidPluginException {
        String filename = System.getProperty("user.dir") + "/src/test/resources/8080-cpu.jar";
        APITest.assignEmuStudioPassword();
        PluginLoader instance = PluginLoader.getInstance();
        Class<Plugin> result = instance.loadPlugin(filename, APITest.getEmuStudioPassword());
        assertNotNull(result);
    }
    
    /**
     * Test crucial method for finding plug-ins' main interface.
     */
    public void testDoesImplement() {
        // test for nested interface
        assertFalse(PluginLoader.doesImplement(CPUListenerStub.class, Plugin.class));
        // test for inherited interface
        assertTrue(PluginLoader.doesImplement(CPUImplStub.class, Plugin.class));
    }
    
    public void testTrustedPlugin() {
        assertTrue(PluginLoader.trustedPlugin(CPUImplStub.class));
    }

}
