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

import emulib.JarCreator;
import emulib.emustudio.APITest;
import emulib.plugins.Plugin;
import emulib.plugins.cpu.CPU;
import emulib.runtime.stubs.CPUImplStub;
import emulib.runtime.stubs.CPUListenerStub;
import emulib.runtime.stubs.UnannotatedCPUStub;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PluginLoaderTest {
    private static final String GOOD_PLUGIN_PATH = "plugin-valid.jar";
    private static final String BAD_PLUGIN_PATH = "plugin-invalid.jar";
    private static final String NOT_A_PLUGIN_PATH = "not-a-plugin.jar";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private PluginLoader pluginLoader;

    @Before
    public void setUp() throws MalformedURLException {
        APITest.assignEmuStudioPassword();
        pluginLoader = new PluginLoader();
    }

    private File toFile(String filename) throws URISyntaxException {
        return new File(getClass().getClassLoader().getResource(filename).toURI());
    }

    private Collection<Class<Plugin>> loadGoodPlugin(PluginLoader instance) throws Exception {
        return instance.loadPlugins(APITest.getEmuStudioPassword(), toFile(GOOD_PLUGIN_PATH));
    }

    private Collection<Class<Plugin>> loadBadPlugin(PluginLoader instance) throws Exception {
        return instance.loadPlugins(APITest.getEmuStudioPassword(), toFile(BAD_PLUGIN_PATH));
    }

    @Test
    public void testLoadJAR() throws Exception {
        Collection<Class<Plugin>> result = loadGoodPlugin(pluginLoader);

        assertEquals(1, result.size());
        for (Class<Plugin> pluginClass : result) {
            Constructor<Plugin> constructor = pluginClass.getConstructor(Long.class, ContextPool.class);
            constructor.newInstance(0L, EasyMock.createNiceMock(ContextPool.class));
        }
    }

    @Test(expected = InvalidPluginException.class)
    public void testLoadNotAPlugin() throws Exception {
        pluginLoader.loadPlugins(APITest.getEmuStudioPassword(), toFile(NOT_A_PLUGIN_PATH));
    }

    @Test
    public void testDoesImplement() {
        // test for nested interface
        assertFalse(PluginLoader.doesImplement(CPUListenerStub.class, Plugin.class));
        // test for inherited interface
        assertTrue(PluginLoader.doesImplement(CPUImplStub.class, Plugin.class));
    }

    @Test
    public void testCorrectTrustedPlugin() {
        assertTrue(PluginLoader.trustedPlugin(CPUImplStub.class));
    }

    @Test
    public void testTrustedPluginOnNotAPluginClassReturnsFalse() {
        assertFalse(PluginLoader.trustedPlugin(CPUListenerStub.class));
    }

    @Test
    public void testTrustedPluginOnInterfaceReturnsFalse() {
        assertFalse(PluginLoader.trustedPlugin(CPU.class));
    }

    @Test
    public void testTrustedPluginOnPluginClassWithoutAnnotation() {
        assertFalse(PluginLoader.trustedPlugin(UnannotatedCPUStub.class));
    }

    @Test(expected = NullPointerException.class)
    public void testLoadPluginNullFileNameThrows() throws Exception {
        pluginLoader.loadPlugins(APITest.getEmuStudioPassword(), null);
    }

    @Test(expected = Throwable.class)
    public void testInvalidPluginConstructorThrows() throws Exception {
        Collection<Class<Plugin>> result = loadBadPlugin(pluginLoader);

        for (Class<Plugin> pluginClass : result) {
            pluginClass.getConstructor(Long.class, ContextPool.class);
        }
    }

    private File createJar(String className, String... dependsOn) throws IOException, URISyntaxException {
        File file = temporaryFolder.newFile(className.replaceAll("/",".").concat(".jar"));
        JarCreator jarCreator = new JarCreator();

        file.getParentFile().mkdirs();
        file.createNewFile();
        jarCreator.createJar(file, toFile(className), Arrays.asList(dependsOn));

        return file;
    }

    @Test
    public void testDependenciesAreLoadedCorrectly() throws Exception {
        System.setProperty("sun.misc.URLClassPath.debugLookupCache", "true");

        File lastDep = createJar("dependencies/hidden/C.class", "");
        File secondDep = createJar("dependencies/hidden/BdependsOnC.class", lastDep.getAbsolutePath());
        File plugin = createJar("dependencies/APluginDependsOnB.class", secondDep.getAbsolutePath());

        // Since PluginLoader must share ClassLoader with current one, emuLib is preloaded automatically
        pluginLoader = new PluginLoader(plugin.getParentFile().getPath());
        Class<Plugin> cl = pluginLoader.loadPlugins(APITest.getEmuStudioPassword(), plugin).iterator().next();

        Constructor<Plugin> constructor = cl.getDeclaredConstructor(Long.class, ContextPool.class);
        cl.getDeclaredMethod("hi").invoke(constructor.newInstance(new Long(0), new ContextPool()));
    }

    @Test(expected = InvalidPasswordException.class)
    public void testLoadPluginInvalidPassword() throws Exception {
        pluginLoader.loadPlugins(APITest.getEmuStudioPassword() + "hahaha", toFile(GOOD_PLUGIN_PATH));

    }
}
