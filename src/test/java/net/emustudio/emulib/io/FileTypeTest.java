/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.io;

import org.junit.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FileTypeTest {
    private final FileType asm = new FileType("Assembler", "asm", "s");

    @Test
    public void testMatchesIgnoresCase() {
        assertTrue(asm.matches(Path.of("dir/program.ASM")));
        assertTrue(asm.matches(Path.of("program.s")));
        assertFalse(asm.matches(Path.of("program.hex")));
        assertFalse(asm.matches(Path.of("asm")));
    }

    @Test
    public void testReplaceExtension() {
        assertEquals(Path.of("dir/program.hex"), FileType.replaceExtension(Path.of("dir/program.asm"), List.of(asm), "hex"));
        assertEquals(Path.of("program.hex"), FileType.replaceExtension(Path.of("program.s"), List.of(asm), "hex"));
        assertEquals(Path.of("program.txt.hex"), FileType.replaceExtension(Path.of("program.txt"), List.of(asm), "hex"));
    }

    @Test
    public void testGetters() {
        assertEquals("Assembler", asm.getDescription());
        assertEquals(List.of("asm", "s"), asm.getExtensions());
        assertEquals(new FileType("Assembler", "asm", "s"), asm);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoExtensionsIsRejected() {
        new FileType("Nothing");
    }
}
