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

import emulib.emustudio.APITest;
import emulib.plugins.Plugin;
import emulib.plugins.cpu.CPU;
import emulib.runtime.stubs.CPUImplStub;
import emulib.runtime.stubs.CPUListenerStub;
import emulib.runtime.stubs.UnannotatedCPUStub;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PluginLoaderTest {
    private static final String GOOD_PLUGIN_PATH = "plugin-valid.jar";
    private static final String BAD_PLUGIN_PATH = "plugin-invalid.jar";
    private static final String NOT_A_PLUGIN_PATH = "not-a-plugin.jar";
    private static final String DEPENDENT_PLUGIN_PATH = "dependencies" + File.separator;
    private static final String PLUGIN_A_DEPENDS_ON_B = DEPENDENT_PLUGIN_PATH + "A.jar";

    private PluginLoader pluginLoader;

    @Before
    public void setUp() throws MalformedURLException {
        APITest.assignEmuStudioPassword();
        pluginLoader = new PluginLoader();
    }

    private File toFile(String filename) throws URISyntaxException {
        return new File(getClass().getClassLoader().getResource(filename).toURI());
    }

    private Class<Plugin> loadGoodPlugin(PluginLoader instance) throws Exception {
        return instance.loadPlugin(toFile(GOOD_PLUGIN_PATH), APITest.getEmuStudioPassword());
    }

    private Class<Plugin> loadBadPlugin(PluginLoader instance) throws Exception {
        return instance.loadPlugin(toFile(BAD_PLUGIN_PATH), APITest.getEmuStudioPassword());
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
        pluginLoader.loadPlugin(toFile(NOT_A_PLUGIN_PATH), APITest.getEmuStudioPassword());
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
        pluginLoader.loadPlugin(null, APITest.getEmuStudioPassword());
    }

    @Test
    public void testFindExistingResourceReturnsCorrectURL() throws Exception {
        loadGoodPlugin(pluginLoader);

        String resource = "/META-INF/maven/net.sf.emustudio/brainduck-cpu/pom.xml";
        URL foundURL = pluginLoader.findResource(resource);

        String file = foundURL.getFile();

        assertEquals(resource, file.substring(file.lastIndexOf('!') + 1));
    }

    @Test
    public void testNonExistantResourceReturnsNullAndDoesNotThrow() throws Exception {
        loadGoodPlugin(pluginLoader);
        URL foundURL = pluginLoader.findResource("non-existent-resource");
        assertNull(foundURL);
    }

    @Test(expected = Throwable.class)
    public void testInvalidPluginConstructorThrows() throws Exception {
        Class<Plugin> result = loadBadPlugin(pluginLoader);

        assertNotNull(result);
        result.getConstructor(Long.class, ContextPool.class);
    }

    @Test
    public void testURLFromConstructorLoadsPlugin() throws Exception {
        URL fileURL = toFile(GOOD_PLUGIN_PATH).toURI().toURL();
        String tmp = "jar:" + fileURL.toString() + "!/";

        pluginLoader = new PluginLoader(new URL(tmp));

        // The JAR file should be loaded automatically
        URL foundURL = pluginLoader.findResource("/META-INF/maven/net.sf.emustudio/brainduck-cpu/pom.xml");
        assertNotNull(foundURL);
    }

    @Test
    public void testDependenciesAreLoadedCorrectly() throws Exception {
        File file = toFile(PLUGIN_A_DEPENDS_ON_B);
        File base = file.getParentFile();

        Class<Plugin> cl = pluginLoader.loadPlugin(
                toFile(PLUGIN_A_DEPENDS_ON_B),
                APITest.getEmuStudioPassword(),
                base.getPath()
        );
        cl.getDeclaredMethod("hello").invoke(cl.newInstance());
    }
}
