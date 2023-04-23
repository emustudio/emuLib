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
import java.util.Optional;

/**
 * Messages are passed to compiler listeners when the compiler wishes to say something.
 */
public class CompilerMessage {

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
    private final Optional<SourceCodePosition> position;

    /**
     * This constructor creates the Message object. Messages are created by
     * compiler.
     *
     * @param messageType Type of the message
     * @param message     Text of the message
     * @param position    Source code position (nullable)
     */
    public CompilerMessage(MessageType messageType, String message, SourceCodePosition position) {
        this.messageType = Objects.requireNonNull(messageType);
        this.message = Objects.requireNonNull(message);
        this.position = Optional.ofNullable(position);
    }

    /**
     * This constructor creates the Message object. Messages are created by
     * compiler.
     *
     * @param message Text of the message
     */
    public CompilerMessage(String message) {
        this(MessageType.TYPE_UNKNOWN, message, null);
    }

    /**
     * This constructor creates the Message object. Messages are created by
     * compiler.
     *
     * @param type    Type of the message.
     * @param message Text of the message
     */
    public CompilerMessage(MessageType type, String message) {
        this(type, message, null);
    }

    /**
     * Return formatted string that represents this Message object.
     *
     * @return formatted message
     */
    public String getFormattedMessage() {
        StringBuilder mes = new StringBuilder();
        position.ifPresent(mes::append);
        position.ifPresent(x -> mes.append(" "));
        mes.append(message);
        return mes.toString();
    }

    /**
     * Get source code position
     *
     * @return position in the source code
     */
    public Optional<SourceCodePosition> getPosition() {
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
