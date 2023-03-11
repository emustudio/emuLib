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

import net.emustudio.emulib.plugins.Plugin;

import java.util.List;

/**
 * Compiler plugin root interface.
 * <p>
 * Should be implemented by a plugin. There should exist just one implementation.
 * <p>
 * Compiler can define a "compiler context", which can specify e.g. a format of compiled file, or other additional
 * meta-information for runtime, which can be used by other plugins.
 *
 * @see CompilerContext
 */
@SuppressWarnings("unused")
public interface Compiler extends Plugin {

    /**
     * Adds CompilerListener object into list of listeners.
     * If some event is occurred in the compiler, listeners methods will be
     * called.
     *
     * @param listener CompilerListener object
     */
    void addCompilerListener(CompilerListener listener);

    /**
     * Removes CompilerListener object from the listeners list.
     *
     * @param listener CompilerListener object
     */
    void removeCompilerListener(CompilerListener listener);

    /**
     * Compile an input file into the output file.
     *
     * @param inputFileName  name of the input file (source code)
     * @param outputFileName name of the output file (compiled code)
     * @return true if compile was successful, false otherwise
     */
    boolean compile(String inputFileName, String outputFileName);

    /**
     * Compile an input file into the output file.
     * <p>
     * Output file name is derived by the compiler itself. Usually, the
     * extension of the input file is replaced by another one, denoting
     * compiled file. It is compiler-specific.
     *
     * @param inputFileName name of the input file (source code)
     * @return true if compile was successful, false otherwise
     */
    boolean compile(String inputFileName);

    /**
     * Creates a lexical analyzer.
     *
     * @return new lexer object
     */
    LexicalAnalyzer createLexer();

    /**
     * Gets location of compiled program in memory.
     * <p>
     * Program location should be valid only after successful compilation.
     * The location should point to the starting position of the program in memory, where the CPU can start emulating.
     *
     * @return location of compiled program in memory
     */
    int getProgramLocation();

    /**
     * Get the list of source file extensions supported by the compiler.
     *
     * @return list of supported source file extensions
     */
    List<FileExtension> getSourceFileExtensions();
}

