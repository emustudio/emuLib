/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.compiler;

import net.emustudio.emulib.plugins.compiler.CompilerMessage.MessageType;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

public class CompilerMessageTest {
    private static final String MESSAGE = "Sample message";
    private static final MessageType MESSAGE_TYPE = MessageType.TYPE_INFO;
    private static final int LINE = 34;
    private static final int COLUMN = 24;
    private static final SourceCodePosition position = new SourceCodePosition(LINE, COLUMN, "");

    @Test
    public void testDefaultValues() {
        CompilerMessage compilerMessage = new CompilerMessage(MESSAGE);

        assertEquals(MessageType.TYPE_UNKNOWN, compilerMessage.getMessageType());
        assertEquals(MESSAGE, compilerMessage.getMessage());
        assertEquals(Optional.empty(), compilerMessage.getPosition());
    }

    @Test
    public void testGetAllValues() {
        CompilerMessage compilerMessage = new CompilerMessage(MESSAGE_TYPE, MESSAGE, position);

        assertEquals(MESSAGE_TYPE, compilerMessage.getMessageType());
        assertEquals(MESSAGE, compilerMessage.getMessage());
        assertEquals(Optional.of(position), compilerMessage.getPosition());
    }

    @Test
    public void testMessageAndType() {
        CompilerMessage compilerMessage = new CompilerMessage(MESSAGE_TYPE, MESSAGE);

        assertEquals(MESSAGE_TYPE, compilerMessage.getMessageType());
        assertEquals(MESSAGE, compilerMessage.getMessage());
    }

    @Test
    public void testMessageTypeAndErrorCode() {
        CompilerMessage compilerMessage = new CompilerMessage(MESSAGE_TYPE, MESSAGE);

        assertEquals(MESSAGE_TYPE, compilerMessage.getMessageType());
        assertEquals(MESSAGE, compilerMessage.getMessage());
    }

    @Test
    public void testMessageTypeSourceFileAndErrorCode() {
        CompilerMessage compilerMessage = new CompilerMessage(MESSAGE_TYPE, MESSAGE);

        assertEquals(MESSAGE_TYPE, compilerMessage.getMessageType());
        assertEquals(MESSAGE, compilerMessage.getMessage());
    }

    @Test
    public void testFormatingForMessage() {
        CompilerMessage compilerMessage = new CompilerMessage(MESSAGE);
        assertEquals(MESSAGE, compilerMessage.getFormattedMessage());
    }

    @Test
    public void testFormattingForInfoMessage() {
        CompilerMessage compilerMessage = new CompilerMessage(MessageType.TYPE_INFO, MESSAGE);
        assertEquals(MESSAGE, compilerMessage.getFormattedMessage());
    }

    @Test
    public void testFormattingForErrorMessage() {
        CompilerMessage compilerMessage = new CompilerMessage(MessageType.TYPE_ERROR, MESSAGE);
        assertEquals(MESSAGE, compilerMessage.getFormattedMessage());
    }

    @Test
    public void testFormattingForWarningMessage() {
        CompilerMessage compilerMessage = new CompilerMessage(MessageType.TYPE_WARNING, MESSAGE);
        assertEquals(MESSAGE, compilerMessage.getFormattedMessage());
    }

    @Test
    public void testFormattedMessageEqualsToString() {
        CompilerMessage compilerMessage = new CompilerMessage(MESSAGE);
        assertEquals(compilerMessage.getFormattedMessage(), compilerMessage.toString());
    }

    @Test
    public void testFormattingForEverything() {
        CompilerMessage compilerMessage = new CompilerMessage(MessageType.TYPE_INFO, MESSAGE, position);

        String expected = position + " " + MESSAGE;
        assertEquals(expected, compilerMessage.getFormattedMessage());
    }
}
