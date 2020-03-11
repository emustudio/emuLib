// SPDX-License-Identifier: GPL-3.0-or-later
package net.emustudio.emulib.plugins.compiler;

import java.util.Objects;

/**
 * Messages are passed to compiler listeners when the compiler wishes to say something.
 */
public class CompilerMessage {
    public static final String MSG_INFO =    "[Info   ] ";
    public static final String MSG_ERROR =   "[Error  ] ";
    public static final String MSG_WARNING = "[Warning] ";

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
    private final int line;
    private final int column;

    /**
     * This constructor creates the Message object. Messages are created by
     * compiler.
     * @param messageType
     *   Type of the message.
     * @param message
     *   Text of the message
     * @param line
     *   Line in the source code
     * @param column
     *   Column in the source code
     */
    public CompilerMessage(MessageType messageType, String message, int line, int column) {
        this.messageType = Objects.requireNonNull(messageType);
        this.message = Objects.requireNonNull(message);
        this.line = line;
        this.column = column;
    }

    /**
     * This constructor creates the Message object. Messages are created by
     * compiler.
     * @param message
     *   Text of the message
     */
    public CompilerMessage(String message) {
        this(MessageType.TYPE_UNKNOWN, message,-1,-1);
    }

    /**
     * This constructor creates the Message object. Messages are created by
     * compiler.
     * @param type
     *   Type of the message.
     * @param message
     *   Text of the message
     */
    public CompilerMessage(MessageType type, String message) {
        this(type,message,-1,-1);
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

        if ((line >= 0) || (column >= 0)) {
            mes.append(String.format(POSITION_FORMAT, line, column));
        }
        mes.append(message);
        return mes.toString();
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

    @Override
    public String toString() {
        return getFormattedMessage();
    }
}
