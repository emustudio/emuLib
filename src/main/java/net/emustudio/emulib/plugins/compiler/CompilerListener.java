/*
 * Run-time library for emuStudio and plug-ins.
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
