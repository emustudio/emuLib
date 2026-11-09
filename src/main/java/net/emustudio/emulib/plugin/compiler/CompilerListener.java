/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugin.compiler;

/**
 * Compiler listener. Only {@link #onMessage(CompilerMessage)} is mandatory.
 */
@FunctionalInterface
public interface CompilerListener {

    /**
     * Called when a compilation begins.
     */
    default void onStart() {
    }

    /**
     * Called when the compiler emits a message.
     *
     * @param message message from the compiler
     */
    void onMessage(CompilerMessage message);

    /**
     * Called when a compilation finishes (successfully or not).
     */
    default void onFinish() {
    }
}
