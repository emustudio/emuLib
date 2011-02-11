/*
 * Message.java
 *
 * (c) Copyright 2011, P. Jakubčo <pjakubco@gmail.com>
 *
 * KISS, YAGNI
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package emuLib8.plugins.compiler;

/**
 * This class provides the message object that will be passed when the compiler
 * wishes to print a message. Compilers that generate messages should use this
 * object.
 *
 * @author vbmacher
 */
public class Message {

    public enum MessageType {
        /**
        * The message represents a warning.
        */
        TYPE_WARNING,
        /**
        * The message represents an error.
        */
        TYPE_ERROR,
        /**
        * The message represents an information.
        */
        TYPE_INFO,
        /**
        * The message is of unknown type.
        */
        TYPE_UNKNOWN
    };

    private MessageType messageType;
    private String message;
    private String sourceFile;
    private int line;
    private int column;
    private int errorCode;

    /**
     * This constructor creates the Message object. Messages are created by
     * compiler.
     * @param message_type
     *   Type of the message.
     * @param message
     *   Text of the message
     * @param line
     *   Line in the source code
     * @param column
     *   Column in the source code
     * @param sourceFile
     *   Name of the file that the message belongs to
     * @param errorCode
     */
    public Message(MessageType message_type, String message, int line, int column,
            String sourceFile, int error_code) {
        this.messageType = message_type;
        this.message = message;
        this.sourceFile = sourceFile;
        this.line = line;
        this.column = column;
        this.errorCode = error_code;
    }

    /**
     * This constructor creates the Message object. Messages are created by
     * compiler.
     * @param message
     *   Text of the message
     */
    public Message(String message) {
        this(MessageType.TYPE_UNKNOWN,message,-1,-1,null, 0);
    }

    /**
     * This constructor creates the Message object. Messages are created by
     * compiler.
     * @param message_type
     *   Type of the message.
     * @param message
     *   Text of the message
     */
    public Message(MessageType type, String message) {
        this(type,message,-1,-1,null, 0);
    }

    /**
     * This constructor creates the Message object. Messages are created by
     * compiler.
     * @param message_type
     *   Type of the message.
     * @param message
     *   Text of the message
     * @param error_code
     *   Error-specific code
     */
    public Message(MessageType message_type, String message, int error_code) {
        this(message_type,message,-1,-1,null,error_code);
    }

    /**
     * This constructor creates the Message object. Messages are created by
     * compiler.
     * @param message_type
     *   Type of the message.
     * @param message
     *   Text of the message
     * @param sourceFile
     *   Name of the file that the message belongs to
     * @param errorCode
     *   Error-specific code
     */
    public Message(MessageType message_type, String message, String sourceFile,
            int errorCode) {
        this(message_type,message,-1,-1,null, errorCode);
    }

    /**
     * Return formatted string that represents this Message object.
     *
     * @return formatted message
     */
    public String getForrmattedMessage() {
        String mes = "";
        switch (messageType) {
            case TYPE_WARNING:
                mes += "[Warning (" + errorCode + ")] ";
                break;
            case TYPE_ERROR:
                mes += "[Error (" + errorCode + ")]   ";
                break;
            case TYPE_INFO:
                mes += "[Info (" + errorCode + ")]    ";
                break;
        }
        if (sourceFile != null)
            mes += sourceFile;
        if ((line >= 0) || (column >= 0)) {
            mes += "(";
            mes += (line >= 0) ? String.valueOf(line) : "?";
            mes += ";";
            mes += (column >= 0) ? String.valueOf(column) : "?";
            mes += ")";
        }
        mes += " " + message;
        return mes;
    }

    /**
     * Get line of the source code that the message belongs to.
     *
     * @return the line, starting from 0. Negative values indicate that this
     * value is not valid.
     */
    public int getLine() {
        return line;
    }

    /**
     * Get column of the source code that the message belongs to.
     *
     * @return the column, starting from 0. Negative values indicate that this
     * value is not valid.
     */
    public int getColumn() {
        return column;
    }

    /**
     * Get the text of the message.
     *
     * @return text of the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get the type of the message.
     * @return the message type
     */
    public MessageType getMessageType() {
        return messageType;
    }
}
