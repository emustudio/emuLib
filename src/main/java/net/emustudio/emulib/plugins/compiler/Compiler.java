// SPDX-License-Identifier: GPL-3.0-or-later
package net.emustudio.emulib.plugins.compiler;

import net.emustudio.emulib.plugins.Plugin;

import java.io.Reader;
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
     * Get a lexical analyzer of the compiler. It is used by main module for
     * syntax highlighting. In the compilation process the compiler should
     * use own, independent lexical analyzer.
     *
     * @param in <code>Reader</code> object of the document - source code.
     * @return lexical analyzer object
     */
    LexicalAnalyzer getLexer(Reader in);

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
    List<SourceFileExtension> getSourceFileExtensions();
}

