// SPDX-License-Identifier: GPL-3.0-or-later
package net.emustudio.emulib.plugins.compiler;

import org.junit.Test;
import static org.junit.Assert.*;

public class SourceFileExtensionTest {
    
    @Test
    public void testSetGetExtensionAndDescription() {
        SourceFileExtension instance = new SourceFileExtension("bmp", "descr");
        assertEquals("bmp", instance.getExtension());
        assertEquals("descr", instance.getDescription());
        assertEquals("descr (*.bmp)", instance.getFormattedDescription());
    }

}
