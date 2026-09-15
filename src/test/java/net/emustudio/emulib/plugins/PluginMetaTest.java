/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins;

import net.emustudio.emulib.plugins.meta.SamplePlugin;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PluginMetaTest {

    @Test
    public void testVersionAndCopyrightReadFromPackageBundle() {
        assertEquals("9.9.9", PluginMeta.version(SamplePlugin.class));
        assertEquals("© 2026 ACME", PluginMeta.copyright(SamplePlugin.class));
    }

    @Test
    public void testMissingBundleReturnsUnknown() {
        assertEquals(PluginMeta.UNKNOWN, PluginMeta.version(PluginMetaTest.class));
        assertEquals(PluginMeta.UNKNOWN, PluginMeta.copyright(PluginMetaTest.class));
    }
}
