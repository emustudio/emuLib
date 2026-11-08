/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.compiler;

import java.util.Objects;

/**
 * Compilation error at a source position. {@link AbstractCompiler} reports it as an error message.
 */
public class CompileException extends RuntimeException {
    public final SourceCodePosition position;

    /**
     * @param position source position
     * @param message  error message
     */
    public CompileException(SourceCodePosition position, String message) {
        this(position, message, null);
    }

    /**
     * @param position source position
     * @param message  error message
     * @param cause    the cause
     */
    public CompileException(SourceCodePosition position, String message, Throwable cause) {
        super(message, cause);
        this.position = Objects.requireNonNull(position);
    }
}
