/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugin.compiler;

import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class CompilerMessageTest {
    private final SourceCodePosition position = new SourceCodePosition(3, 4, "file.asm");

    @Test
    public void testMessageWithPosition() {
        CompilerMessage message = new CompilerMessage(CompilerMessage.Type.ERROR, "msg", position);

        assertEquals(CompilerMessage.Type.ERROR, message.getType());
        assertEquals("msg", message.getMessage());
        assertEquals(Optional.of(position), message.getPosition());
        assertEquals(position + " msg", message.getFormattedMessage());
        assertEquals(message.getFormattedMessage(), message.toString());
    }

    @Test
    public void testMessageWithoutPosition() {
        CompilerMessage message = new CompilerMessage(CompilerMessage.Type.INFO, "msg", null);

        assertEquals(Optional.empty(), message.getPosition());
        assertEquals("msg", message.getFormattedMessage());
    }

    @Test(expected = NullPointerException.class)
    public void testNullMessageIsRejected() {
        new CompilerMessage(CompilerMessage.Type.INFO, null, null);
    }
}
