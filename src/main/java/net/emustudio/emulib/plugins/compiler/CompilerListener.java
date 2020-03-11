// SPDX-License-Identifier: GPL-3.0-or-later
package net.emustudio.emulib.plugins.compiler;

/**
 * This is an interface that should be implemented by the emuStudio,
 * and/or other plugins that want to process the output of the compiler.
 */
public interface CompilerListener {
    /**
     * This method is called whenever a compiler begins to work.
     */
    void onStart();

    /**
     * Method will be invoked when compiler would like to print info message.
     *
     * @param compilerMessage Message from the compiler
     */
    void onMessage(CompilerMessage compilerMessage);

    /**
     * This method is called whenever the compiler finishes the compilation.
     */
    void onFinish();
}
