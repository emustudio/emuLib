/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.compiler.antlr;

import net.emustudio.emulib.plugins.compiler.CompileException;
import net.emustudio.emulib.plugins.compiler.SourceCodePosition;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ParserErrorListenerTest {

    @Test
    public void testSyntaxErrorThrowsCompileException() {
        try {
            new ParserErrorListener("file.asm").syntaxError(null, null, 3, 7, "bad input", null);
            fail("Expected CompileException");
        } catch (CompileException e) {
            assertEquals(new SourceCodePosition(3, 7, "file.asm"), e.position);
            assertEquals("bad input", e.getMessage());
        }
    }
}
