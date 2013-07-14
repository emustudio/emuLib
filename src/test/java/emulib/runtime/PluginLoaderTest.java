/*
 * PluginLoaderTest.java
 *
 * KISS, YAGNI, DRY
 *
 * (c) Copyright 2010-2013, Peter Jakubčo
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
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import javax.swing.JPanel;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class PluginLoaderTest {

    private class CPUListenerStub implements CPUListener {
        @Override
        public void runStateChanged(RunState runState) {}
        @Override
        public void internalStateChanged() {}
    }

    private abstract class SuperCPUStub implements CPU { }

    @PluginType(title="CPU", description="", type=PLUGIN_TYPE.CPU, copyright="(c)")
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

    @Before
    public void setUp() {
        APITest.assignEmuStudioPassword();
    }

    @After
    public void tearDown() throws InvalidPasswordException {
      PluginLoader.getInstance().forgetAllLoaded(APITest.getEmuStudioPassword());
    }

    /**
     * Test of getInstance method, of class Loader.
     */
    @Test
    public void testGetInstance() {
        PluginLoader expResult = PluginLoader.getInstance();
        assertEquals(expResult, PluginLoader.getInstance());
    }

    private Class<Plugin> loadGoodPlugin(PluginLoader instance) throws InvalidPasswordException, InvalidPluginException {
        String filename = System.getProperty("user.dir") + "/src/test/resources/8080-cpu.jar";
        return instance.loadPlugin(filename, APITest.getEmuStudioPassword());
    }

    private Class<Plugin> loadBadPlugin(PluginLoader instance) throws InvalidPasswordException, InvalidPluginException {
        String filename = System.getProperty("user.dir") + "/src/test/resources/ramc-ram.jar";
        PluginLoader.getInstance();
        return instance.loadPlugin(filename, APITest.getEmuStudioPassword());
    }

    @Test
    public void testLoadJAR() throws InvalidPasswordException, InvalidPluginException, PluginNotFullyLoadedException {
        PluginLoader instance = PluginLoader.getInstance();
        Class<Plugin> result = loadGoodPlugin(instance);
        assertNotNull(result);
        assertTrue(instance.isEverythingLoaded(APITest.getEmuStudioPassword()));
        assertTrue(instance.geRemainingJARFiles(APITest.getEmuStudioPassword()).isEmpty());
        instance.resolveLoadedClasses(APITest.getEmuStudioPassword());
    }

    /**
     * Test crucial method for finding plug-ins' main interface.
     */
    @Test
    public void testDoesImplement() {
        // test for nested interface
        assertFalse(PluginLoader.doesImplement(CPUListenerStub.class, Plugin.class));
        // test for inherited interface
        assertTrue(PluginLoader.doesImplement(CPUImplStub.class, Plugin.class));
    }

    @Test
    public void testTrustedPlugin() {
        assertTrue(PluginLoader.trustedPlugin(CPUImplStub.class));
        assertFalse(PluginLoader.trustedPlugin(CPUListenerStub.class));
    }

    @Test(expected = InvalidPluginException.class)
    public void testLoadPluginNullFileName() throws InvalidPasswordException, InvalidPluginException {
        PluginLoader instance = PluginLoader.getInstance();
        instance.loadPlugin(null, APITest.getEmuStudioPassword());
    }

    @Test
    public void testNotEverythingLoadedWithIncompleteJARFile() throws InvalidPasswordException, InvalidPluginException {
        PluginLoader instance = PluginLoader.getInstance();
        Class<Plugin> result = loadBadPlugin(instance);
        assertNotNull(result);
        assertFalse(instance.isEverythingLoaded(APITest.getEmuStudioPassword()));

        List<PluginLoader.NotLoadedJAR> notLoadedJARs = instance.geRemainingJARFiles(APITest.getEmuStudioPassword());
        assertTrue(notLoadedJARs.size() > 0);
        for (PluginLoader.NotLoadedJAR notLoadedJAR: notLoadedJARs) {
            System.out.println("NLJ: fileName=" + notLoadedJAR.filename + ", undone="
                    + Arrays.toString(notLoadedJAR.getUndone().toArray()));
        }
    }

    @Test(expected = PluginNotFullyLoadedException.class)
    public void testResolveWillThrowWithIncompleteJARFile() throws InvalidPasswordException, InvalidPluginException, PluginNotFullyLoadedException {
        PluginLoader instance = PluginLoader.getInstance();
        loadBadPlugin(instance);
        assertFalse(instance.isEverythingLoaded(APITest.getEmuStudioPassword()));
        instance.resolveLoadedClasses(APITest.getEmuStudioPassword());
    }

    @Test(expected = PluginNotFullyLoadedException.class)
    public void testLoadRemainingJARFilesWillThrow() throws InvalidPasswordException, InvalidPluginException, PluginNotFullyLoadedException {
        PluginLoader instance = PluginLoader.getInstance();
        loadBadPlugin(instance);
        assertFalse(instance.isEverythingLoaded(APITest.getEmuStudioPassword()));
        instance.loadRemainingJARFiles(APITest.getEmuStudioPassword());
    }

    @Test
    public void testFindResource() throws InvalidPasswordException, InvalidPluginException {
        PluginLoader instance = PluginLoader.getInstance();
        loadGoodPlugin(instance);
        URL foundURL = instance.findResource("/META-INF/maven/net.sf.emustudio/8080-cpu/pom.xml");
        assertNotNull(foundURL);
    }
}
