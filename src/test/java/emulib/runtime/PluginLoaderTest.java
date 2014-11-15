/*
 * KISS, YAGNI, DRY
 *
 * (c) Copyright 2010-2014, Peter Jakubčo
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
import emulib.plugins.PluginInitializationException;
import emulib.plugins.cpu.CPU;
import emulib.plugins.cpu.CPU.CPUListener;
import emulib.plugins.cpu.CPU.RunState;
import emulib.plugins.cpu.Disassembler;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import javax.swing.JPanel;
import java.io.File;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PluginLoaderTest {
    private static final String RESOURCES_PATH = "src" + File.separator + "test"
            + File.separator + "resources" + File.separator;
    
    private static final String GOOD_PLUGIN_PATH = RESOURCES_PATH + "plugin-valid.jar";
    private static final String BAD_PLUGIN_PATH = RESOURCES_PATH + "plugin-invalid.jar";
    private static final String NOT_A_PLUGIN_PATH = RESOURCES_PATH + "not-a-plugin.jar";
    private static final String DEPENDENT_PLUGIN_PATH = RESOURCES_PATH
            + "dependencies" + File.separator;
    private static final String PLUGIN_A_DEPENDS_ON_B = DEPENDENT_PLUGIN_PATH
            + "A.jar";

    private PluginLoader pluginLoader;

    private class CPUListenerStub implements CPUListener {

        @Override
        public void runStateChanged(RunState runState) {
        }

        @Override
        public void internalStateChanged() {
        }
    }

    private abstract class SuperCPUStub implements CPU {
    }

    @PluginType(title = "CPU", description = "", type = PLUGIN_TYPE.CPU, copyright = "(c)")
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
        public void step() {
        }

        @Override
        public void execute() {
        }

        @Override
        public void pause() {
        }

        @Override
        public void stop() {
        }

        @Override
        public JPanel getStatusPanel() {
            return null;
        }

        @Override
        public boolean isBreakpointSupported() {
            return false;
        }

        @Override
        public void setBreakpoint(int pos) {
        }

        @Override
        public void unsetBreakpoint(int pos) {
        }

        @Override
        public boolean isBreakpointSet(int pos) {
            return false;
        }

        @Override
        public void reset(int startAddress) {
        }

        @Override
        public int getInstructionPosition() {
            return 0;
        }

        @Override
        public boolean setInstructionPosition(int pos) {
            return false;
        }

        @Override
        public Disassembler getDisassembler() {
            return null;
        }

        @Override
        public void reset() {
        }

        @Override
        public void initialize(SettingsManager sHandler) throws PluginInitializationException {
            throw new PluginInitializationException(null);
        }

        @Override
        public void destroy() {
        }

        @Override
        public void showSettings() {
        }

        @Override
        public boolean isShowSettingsSupported() {
            return false;
        }

        @Override
        public String getVersion() {
            return "";
        }

        @Override
        public String getTitle() {
            return CPUImplStub.class.getAnnotation(PluginType.class).title();
        }
    }

    @Before
    public void setUp() throws MalformedURLException {
        APITest.assignEmuStudioPassword();
        pluginLoader = new PluginLoader();
    }

    private Class<Plugin> loadGoodPlugin(PluginLoader instance) throws Exception {
        return instance.loadPlugin(GOOD_PLUGIN_PATH, APITest.getEmuStudioPassword());
    }

    private Class<Plugin> loadBadPlugin(PluginLoader instance) throws Exception {
        return instance.loadPlugin(BAD_PLUGIN_PATH, APITest.getEmuStudioPassword());
    }

    @Test
    public void testLoadJAR() throws Exception {
        Class<Plugin> result = loadGoodPlugin(pluginLoader);
        assertNotNull(result);
        Constructor<Plugin> constructor = result.getConstructor(Long.class, ContextPool.class);
        constructor.newInstance(0L, EasyMock.createNiceMock(ContextPool.class));
    }

    @Test(expected = InvalidPluginException.class)
    public void testLoadNotAPlugin() throws Exception {
        pluginLoader.loadPlugin(NOT_A_PLUGIN_PATH, APITest.getEmuStudioPassword());
    }

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

    @Test(expected = NullPointerException.class)
    public void testLoadPluginNullFileName() throws Exception {
        pluginLoader.loadPlugin(null, APITest.getEmuStudioPassword());
    }

    @Test
    public void testFindResource() throws Exception {
        loadGoodPlugin(pluginLoader);
        URL foundURL = pluginLoader.findResource("/META-INF/maven/net.sf.emustudio/brainduck-cpu/pom.xml");
        assertNotNull(foundURL);
    }

    @Test
    public void testFindNonexistantResource() throws Exception {
        loadGoodPlugin(pluginLoader);
        URL foundURL = pluginLoader.findResource("non-existent-resource");
        assertNull(foundURL);
    }

    @Test(expected = NoClassDefFoundError.class)
    public void testBadPlugin() throws Exception {
        Class<Plugin> result = loadBadPlugin(pluginLoader);
        assertNotNull(result);
        Constructor<Plugin> constructor = result.getConstructor(Long.class, ContextPool.class);
        constructor.newInstance(0, EasyMock.createNiceMock(ContextPool.class));
    }

    @Test
    public void testURLFromConstructor() throws MalformedURLException {
        URL fileURL = new File(GOOD_PLUGIN_PATH).toURI().toURL();
        String tmp = "jar:" + fileURL.toString() + "!/";

        pluginLoader = new PluginLoader(new URL(tmp));

        // The JAR file should be loaded automatically
        URL foundURL = pluginLoader.findResource("/META-INF/maven/net.sf.emustudio/brainduck-cpu/pom.xml");
        assertNotNull(foundURL);
    }

    @Test
    public void testDependenciesAreLoadedCorrectly() throws Exception {
        Class<Plugin> cl = pluginLoader.loadPlugin(
                PLUGIN_A_DEPENDS_ON_B,
                APITest.getEmuStudioPassword(),
                RESOURCES_PATH + "dependencies"
        );
        cl.getDeclaredMethod("hello").invoke(cl.newInstance());
    }
}
