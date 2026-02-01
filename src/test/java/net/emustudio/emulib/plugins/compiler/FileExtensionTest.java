/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.compiler;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static net.emustudio.emulib.plugins.compiler.FileExtension.stripKnownExtension;
import static org.junit.Assert.assertEquals;

public class FileExtensionTest {
    private final List<FileExtension> knownExtensions = List.of(
            new FileExtension("asm", "description"),
            new FileExtension("inc", "description")
    );

    @Test
    public void testSetGetExtensionAndDescription() {
        FileExtension instance = new FileExtension("bmp", "descr");
        assertEquals("bmp", instance.getExtension());
        assertEquals("descr", instance.getDescription());
    }

    @Test
    public void testStripKnownExtension() {
        String result = stripKnownExtension("file.asm", knownExtensions);
        assertEquals("file", result);
    }

    @Test
    public void testStripKnownExtensionCaseDoesNotMatch() {
        String result = stripKnownExtension("file.aSm", knownExtensions);
        assertEquals("file.aSm", result);
    }

    @Test
    public void testStripKnownExtensionEmptyKnownExtensions() {
        String result = stripKnownExtension("file.asm", Collections.emptyList());
        assertEquals("file.asm", result);
    }

    @Test
    public void testStripKnownExtensionSecondOne() {
        String result = stripKnownExtension("file.inc", knownExtensions);
        assertEquals("file", result);
    }
}
