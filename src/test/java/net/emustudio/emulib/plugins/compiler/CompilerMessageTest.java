/*
 * Run-time library for emuStudio and plugins.
 *
 *     Copyright (C) 2006-2020  Peter Jakubčo
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.emustudio.emulib.plugins.compiler;

import net.emustudio.emulib.plugins.compiler.CompilerMessage.MessageType;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

public class CompilerMessageTest {
    private static final String MESSAGE = "Sample message";
    private static final MessageType MESSAGE_TYPE = MessageType.TYPE_INFO;
    private static final String SRC_FILE = "source.asm";
    private static final int LINE = 34;
    private static final int COLUMN = 24;

    @Test
    public void testDefaultValues() {
        CompilerMessage compilerMessage = new CompilerMessage(MESSAGE);
        
        assertEquals(MessageType.TYPE_UNKNOWN, compilerMessage.getMessageType());
        assertEquals(MESSAGE, compilerMessage.getMessage());
        assertEquals(-1, compilerMessage.getLine());
        assertEquals(-1, compilerMessage.getColumn());
        assertTrue(compilerMessage.getSourceFile().isEmpty());
    }
    
    @Test
    public void testGetAllValues() {
        CompilerMessage compilerMessage = new CompilerMessage(MESSAGE_TYPE, MESSAGE, LINE, COLUMN, SRC_FILE);
        
        assertEquals(MESSAGE_TYPE, compilerMessage.getMessageType());
        assertEquals(MESSAGE, compilerMessage.getMessage());
        assertEquals(LINE, compilerMessage.getLine());
        assertEquals(COLUMN, compilerMessage.getColumn());
        assertEquals(Optional.of(SRC_FILE), compilerMessage.getSourceFile());
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
        CompilerMessage compilerMessage = new CompilerMessage(MESSAGE_TYPE, MESSAGE, SRC_FILE);
        
        assertEquals(MESSAGE_TYPE, compilerMessage.getMessageType());
        assertEquals(MESSAGE, compilerMessage.getMessage());
        assertEquals(Optional.of(SRC_FILE), compilerMessage.getSourceFile());
    }
    
    @Test
    public void testFormatingForMessage() {
        CompilerMessage compilerMessage = new CompilerMessage(MESSAGE);
        assertEquals(MESSAGE, compilerMessage.getFormattedMessage());
    }

    @Test
    public void testFormattingForInfoMessage() {
        CompilerMessage compilerMessage = new CompilerMessage(MessageType.TYPE_INFO, MESSAGE);
        assertEquals(CompilerMessage.MSG_INFO + MESSAGE, compilerMessage.getFormattedMessage());
    }

    @Test
    public void testFormattingForErrorMessage() {
        CompilerMessage compilerMessage = new CompilerMessage(MessageType.TYPE_ERROR, MESSAGE);
        assertEquals(CompilerMessage.MSG_ERROR + MESSAGE, compilerMessage.getFormattedMessage());
    }

    @Test
    public void testFormattingForWarningMessage() {
        CompilerMessage compilerMessage = new CompilerMessage(MessageType.TYPE_WARNING, MESSAGE);
        assertEquals(CompilerMessage.MSG_WARNING + MESSAGE, compilerMessage.getFormattedMessage());
    }
    
    @Test
    public void testFormattedMessageEqualsToString() {
        CompilerMessage compilerMessage = new CompilerMessage(MESSAGE);
        assertEquals(compilerMessage.getFormattedMessage(), compilerMessage.toString());
    }
    
    @Test
    public void testFormattingForEverything() {
        CompilerMessage compilerMessage = new CompilerMessage(
            MessageType.TYPE_INFO, MESSAGE, LINE, COLUMN, SRC_FILE
        );
        
        String expected = CompilerMessage.MSG_INFO
                + String.format(CompilerMessage.SOURCE_FILE_FORMAT, SRC_FILE)
                + String.format(CompilerMessage.POSITION_FORMAT, LINE, COLUMN)
                + MESSAGE;
    
        assertEquals(expected, compilerMessage.getFormattedMessage());
    }
}
