/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.compiler;

import java.util.Objects;
import java.util.Optional;

/**
 * Messages are passed to compiler listeners when the compiler wishes to say something.
 */
public final class CompilerMessage {

    public enum Type {
        ERROR, WARNING, INFO
    }

    private final Type type;
    private final String message;
    private final SourceCodePosition position;

    /**
     * @param type     message type
     * @param message  message text
     * @param position source position, or null if the message is not related to a source position
     */
    public CompilerMessage(Type type, String message, SourceCodePosition position) {
        this.type = Objects.requireNonNull(type);
        this.message = Objects.requireNonNull(message);
        this.position = position;
    }

    /**
     * @return message text prefixed with the source position, if present
     */
    public String getFormattedMessage() {
        return position == null ? message : position + " " + message;
    }

    /**
     * @return source position, if present
     */
    public Optional<SourceCodePosition> getPosition() {
        return Optional.ofNullable(position);
    }

    /**
     * @return message text
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return message type
     */
    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return getFormattedMessage();
    }
}
