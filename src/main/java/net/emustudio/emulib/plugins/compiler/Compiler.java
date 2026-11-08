/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.compiler;

import net.emustudio.emulib.plugins.Plugin;
import net.emustudio.emulib.runtime.io.FileType;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Compiler plugin root interface.
 * <p>
 * Should be implemented by a plugin. There should exist just one implementation.
 * Compilers usually extend {@link AbstractCompiler}.
 */
public interface Compiler extends Plugin {

    /**
     * Adds compiler listener.
     *
     * @param listener compiler listener
     */
    void addCompilerListener(CompilerListener listener);

    /**
     * Removes compiler listener.
     *
     * @param listener compiler listener
     */
    void removeCompilerListener(CompilerListener listener);

    /**
     * Compile given input file.
     * <p>
     * If output path is not given, it is derived by replacing the source file extension of the input path.
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
     * Coordinate convention of this compiler's memory source-position annotations. Declaring a
     * known unit also declares one-based lines, zero-based columns, and tabs counting as one
     * column. UNKNOWN means the consumer cannot safely resolve positions to source offsets.
     * Lexical Token offsets remain UTF-16 independently of this convention.
     */
    default SourceCodePosition.ColumnUnit getSourcePositionColumnUnit() {
        return SourceCodePosition.ColumnUnit.UNKNOWN;
    }

    /**
     * Get source file types supported by the compiler.
     *
     * @return supported source file types
     */
    List<FileType> getSourceFileTypes();

    /**
     * Determine if automation is supported.
     *
     * @return true by default
     */
    @Override
    default boolean isAutomationSupported() {
        return true;
    }
}
