/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins;

import net.emustudio.emulib.plugins.meta.SamplePlugin;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

public class PluginTest {
    @Rule
    public TemporaryFolder temporary = new TemporaryFolder();

    @Test
    public void testVersionAndCopyrightReadFromPackageBundle() {
        Plugin plugin = new SamplePlugin();
        assertEquals("9.9.9", plugin.getVersion());
        assertEquals("© 2026 ACME", plugin.getCopyright());
    }

    @Test
    public void testMissingBundleReturnsUnknown() {
        Plugin plugin = new SamplePlugin() {};
        assertEquals("(unknown)", plugin.getVersion());
        assertEquals("(unknown)", plugin.getCopyright());
    }

    @Test
    public void testMissingKeyReturnsUnknown() throws Exception {
        assertIsolatedMetadata("version=isolated\n", "isolated", "(unknown)");
    }

    @Test
    public void testMetadataUsesConcretePluginClassLoader() throws Exception {
        assertIsolatedMetadata("version=isolated\ncopyright=plugin JAR\n", "isolated", "plugin JAR");
    }

    @Test
    public void testMetadataCanBeOverridden() {
        Plugin plugin = new SamplePlugin() {
            @Override
            public String getVersion() {
                return "custom version";
            }

            @Override
            public String getCopyright() {
                return "custom copyright";
            }
        };
        assertEquals("custom version", plugin.getVersion());
        assertEquals("custom copyright", plugin.getCopyright());
    }

    private void assertIsolatedMetadata(String metadata, String version, String copyright) throws Exception {
        Path root = temporary.newFolder().toPath();
        Path bundle = root.resolve(SamplePlugin.class.getPackageName().replace('.', '/')).resolve("version.properties");
        Files.createDirectories(bundle.getParent());
        Files.writeString(bundle, metadata);

        try (var loader = new URLClassLoader(new URL[]{root.toUri().toURL()}, Plugin.class.getClassLoader()) {
            @Override
            public URL getResource(String name) {
                return findResource(name);
            }

            Class<?> definePlugin(byte[] bytes) {
                return defineClass(SamplePlugin.class.getName(), bytes, 0, bytes.length);
            }
        }; InputStream bytes = SamplePlugin.class.getResourceAsStream("SamplePlugin.class")) {
            Class<?> pluginClass = loader.definePlugin(bytes.readAllBytes());
            Plugin plugin = (Plugin) pluginClass.getConstructor().newInstance();
            assertNotSame(Plugin.class.getClassLoader(), plugin.getClass().getClassLoader());
            assertEquals(version, plugin.getVersion());
            assertEquals(copyright, plugin.getCopyright());
        }
    }
}
