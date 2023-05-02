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

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static net.emustudio.emulib.plugins.compiler.FileExtension.stripKnownExtension;

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
     * Compile given input file.
     * <p>
     * Output file name (if the compiler has any output and if it is not given here), is derived by the compiler itself.
     * Usually, the extension of the input file is replaced by another one, denoting compiled file. It is compiler-specific.
     * <p>
     * Usually a successfully compiled program is loaded in memory.
     *
     * @param inputPath  input file path (source code)
     * @param outputPath optional output file path
     */
    void compile(Path inputPath, Optional<Path> outputPath);

    /**
     * Creates a lexical analyzer.
     *
     * @return new lexer object
     */
    LexicalAnalyzer createLexer();

    /**
     * Get the list of source file extensions supported by the compiler.
     *
     * @return list of supported source file extensions
     */
    List<FileExtension> getSourceFileExtensions();

    /**
     * Determine if automation is supported.
     *
     * @return true by default
     */
    @Override
    default boolean isAutomationSupported() {
        return true;
    }

    /**
     * Converts input path to output path.
     * <p>
     * The conversion is performed by stripping off known source file extension from the input path
     * (by calling {@link #getSourceFileExtensions()} method), and appending
     * output extension. If the input path doesn't end with any known source file extension, just the output extension
     * is appended to given input path.
     *
     * @param inputPath       input path
     * @param outputExtension output extension (e.g. ".hex")
     * @return input path converted
     */
    default Path convertInputToOutputPath(Path inputPath, String outputExtension) {
        return Path.of(stripKnownExtension(inputPath.toString(), getSourceFileExtensions()) + outputExtension);
    }
}

