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

import java.util.Objects;

/**
 * Messages are passed to compiler listeners when the compiler wishes to say something.
 */
public class CompilerMessage {
    public static final String MSG_INFO = "[INFO   ] ";
    public static final String MSG_ERROR = "[ERROR  ] ";
    public static final String MSG_WARNING = "[WARNING] ";

    public static final String POSITION_FORMAT = "(%3d,%3d) ";

    /**
     * Message type.
     */
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
    }

    private final MessageType messageType;
    private final String message;
    private final SourceCodePosition position;

    /**
     * This constructor creates the Message object. Messages are created by
     * compiler.
     *
     * @param messageType Type of the message.
     * @param message     Text of the message
     * @param line        Line in the source code
     * @param column      Column in the source code
     */
    public CompilerMessage(MessageType messageType, String message, int line, int column) {
        this.messageType = Objects.requireNonNull(messageType);
        this.message = Objects.requireNonNull(message);
        this.position = new SourceCodePosition(line, column);
    }

    /**
     * This constructor creates the Message object. Messages are created by
     * compiler.
     *
     * @param messageType Type of the message
     * @param message     Text of the message
     * @param position    Source code position
     */
    public CompilerMessage(MessageType messageType, String message, SourceCodePosition position) {
        this.messageType = Objects.requireNonNull(messageType);
        this.message = Objects.requireNonNull(message);
        this.position = Objects.requireNonNull(position);
    }

    /**
     * This constructor creates the Message object. Messages are created by
     * compiler.
     *
     * @param message Text of the message
     */
    public CompilerMessage(String message) {
        this(MessageType.TYPE_UNKNOWN, message, -1, -1);
    }

    /**
     * This constructor creates the Message object. Messages are created by
     * compiler.
     *
     * @param type    Type of the message.
     * @param message Text of the message
     */
    public CompilerMessage(MessageType type, String message) {
        this(type, message, -1, -1);
    }

    /**
     * Return formatted string that represents this Message object.
     *
     * @return formatted message
     */
    public String getFormattedMessage() {
        StringBuilder mes = new StringBuilder();
        switch (messageType) {
            case TYPE_WARNING:
                mes.append(MSG_WARNING);
                break;
            case TYPE_ERROR:
                mes.append(MSG_ERROR);
                break;
            case TYPE_INFO:
                mes.append(MSG_INFO);
                break;
        }

        if ((position.line >= 0) || (position.column >= 0)) {
            mes.append(String.format(POSITION_FORMAT, position.line, position.column));
        }
        mes.append(message);
        return mes.toString();
    }

    /**
     * Get source code position
     *
     * @return position in the source code
     */
    public SourceCodePosition getPosition() {
        return position;
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
     *
     * @return the message type
     */
    public MessageType getMessageType() {
        return messageType;
    }

    @Override
    public String toString() {
        return getFormattedMessage();
    }
}
