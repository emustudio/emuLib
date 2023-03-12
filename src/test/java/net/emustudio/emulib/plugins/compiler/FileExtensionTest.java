/*
 * This file is part of emuLib.
 *
 * Copyright (C) 2006-2023  Peter Jakubčo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
