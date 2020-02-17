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

import net.emustudio.emulib.plugins.compiler.Message.MessageType;
import org.junit.Test;
import static org.junit.Assert.*;

public class MessageTest {
    private static final String MESSAGE = "Sample message";
    private static final MessageType MESSAGE_TYPE = MessageType.TYPE_INFO;
    private static final String SRC_FILE = "source.asm";
    private static final int LINE = 34;
    private static final int COLUMN = 24;
    private static final int ERROR_CODE = 5;

    @Test
    public void testDefaultValues() {
        Message message = new Message(MESSAGE);
        
        assertEquals(MessageType.TYPE_UNKNOWN, message.getMessageType());
        assertEquals(MESSAGE, message.getMessage());
        assertEquals(-1, message.getLine());
        assertEquals(-1, message.getColumn());
        assertEquals(0, message.getErrorCode());
        assertEquals(null, message.getSourceFile());
    }
    
    @Test
    public void testGetAllValues() {
        Message message = new Message(MESSAGE_TYPE, MESSAGE, LINE, COLUMN, SRC_FILE, ERROR_CODE);
        
        assertEquals(MESSAGE_TYPE, message.getMessageType());
        assertEquals(MESSAGE, message.getMessage());
        assertEquals(LINE, message.getLine());
        assertEquals(COLUMN, message.getColumn());
        assertEquals(ERROR_CODE, message.getErrorCode());
        assertEquals(SRC_FILE, message.getSourceFile());
    }
    
    @Test
    public void testMessageAndType() {
        Message message = new Message(MESSAGE_TYPE, MESSAGE);
        
        assertEquals(MESSAGE_TYPE, message.getMessageType());
        assertEquals(MESSAGE, message.getMessage());        
    }
    
    @Test
    public void testMessageTypeAndErrorCode() {
        Message message = new Message(MESSAGE_TYPE, MESSAGE, ERROR_CODE);
        
        assertEquals(MESSAGE_TYPE, message.getMessageType());
        assertEquals(MESSAGE, message.getMessage());        
        assertEquals(ERROR_CODE, message.getErrorCode());
    }

    @Test
    public void testMessageTypeSourceFileAndErrorCode() {
        Message message = new Message(MESSAGE_TYPE, MESSAGE, SRC_FILE, ERROR_CODE);
        
        assertEquals(MESSAGE_TYPE, message.getMessageType());
        assertEquals(MESSAGE, message.getMessage());        
        assertEquals(SRC_FILE, message.getSourceFile());
        assertEquals(ERROR_CODE, message.getErrorCode());
    }
    
    @Test
    public void testFormatingForMessage() {
        Message message = new Message(MESSAGE);
        assertEquals(MESSAGE, message.getFormattedMessage());
    }

    @Test
    public void testFormattingForInfoMessage() {
        Message message = new Message(MessageType.TYPE_INFO, MESSAGE);
        assertEquals(String.format(Message.INFO_FORMAT, 0) + MESSAGE, message.getFormattedMessage());
    }

    @Test
    public void testFormattingForErrorMessage() {
        Message message = new Message(MessageType.TYPE_ERROR, MESSAGE);
        assertEquals(String.format(Message.ERROR_FORMAT, 0) + MESSAGE, message.getFormattedMessage());
    }

    @Test
    public void testFormattingForWarningMessage() {
        Message message = new Message(MessageType.TYPE_WARNING, MESSAGE);
        assertEquals(String.format(Message.WARNING_FORMAT, 0) + MESSAGE, message.getFormattedMessage());
    }
    
    @Test
    public void testFormattedMessageEqualsToString() {
        Message message = new Message(MESSAGE);
        assertEquals(message.getFormattedMessage(), message.toString());
    }
    
    @Test
    public void testFormattingForEverything() {
        Message message = new Message(MessageType.TYPE_INFO, MESSAGE, LINE,
                COLUMN, SRC_FILE, ERROR_CODE);
        
        String expected = String.format(Message.INFO_FORMAT, ERROR_CODE)
                + String.format(Message.SOURCE_FILE_FORMAT, SRC_FILE)
                + String.format(Message.POSITION_FORMAT, LINE, COLUMN)
                + MESSAGE;
    
        assertEquals(expected, message.getFormattedMessage());
    }
    
}
