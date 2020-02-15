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
package emulib.plugins.compiler;

import emulib.plugins.Plugin;
import java.io.Reader;

/**
 * This interface is the core for compiler plug-in types. These plug-ins
 * should implement this interface once and only once.
 */
public interface Compiler extends Plugin {

    /**
     * Adds CompilerListener object into list of listeners.
     * If some event is occurred in the compiler, listeners methods will be
     * called.
     *
     * @param listener CompilerListener object
     * @return true if the listener was added, false otherwise
     */
    boolean addCompilerListener (CompilerListener listener);

    /**
     * Removes CompilerListener object from the listeners list.
     *
     * @param listener CompilerListener object
     * @return true if the listener was removed, false otherwise
     */
    boolean removeCompilerListener (CompilerListener listener);

    /**
     * This is an interface that should be implemented by the emuStudio,
     * and/or other plug-ins that want to process the output of the compiler.
     */
    interface CompilerListener {
        /**
         * This method is called whenever a compiler begins to work.
         */
        void onStart ();

        /**
         * Method will be invoked when compiler would like to print info message.
         *
         * @param message Message from the compiler
         */
        void onMessage (Message message);

        /**
         * This method is called whenever the compiler finishes the compilation.
         *
         * @param errorCode compiler-specific error code
         */
        void onFinish (int errorCode);
    }

    /**
     * Compile an input file into the output file.
     * 
     * @param inputFileName  name of the input file (source code)
     * @param outputFileName  name of the output file (compiled code)
     *
     * @return true if compile was successful, false otherwise
     */
    boolean compile (String inputFileName, String outputFileName);

    /**
     * Compile an input file into the output file.
     * 
     * Output file name is derived by the compiler itself. Usually, the 
     * extension of the input file is replaced by another one, denoting
     * compiled file. It is compiler-specific.
     * 
     * @param inputFileName  name of the input file (source code)
     *
     * @return true if compile was successful, false otherwise
     */
    boolean compile (String inputFileName);
    
    /**
     * Get a lexical analyzer of the compiler. It is used by main module for
     * syntax highlighting. In the compilation process the compiler should
     * use own, independent lexical analyzer.
     *
     * @param in  <code>Reader</code> object of the document - source code.
     * @return lexical analyzer object
     */
    LexicalAnalyzer getLexer(Reader in);

    /**
     * Gets starting address of compiled source. It is (or can be) called only
     * after compilation process. It should return the first occurrence of compiled
     * program. The word "address" can be replaced by a term "offset from 0". One
     * step in address has size of one byte. It means that return value should
     * not be related to operating memory units and should not deliberate
     * techniques used in operating memory, e.g. banking.
     *
     * @return address of program's first occurrence
     */
    int getProgramStartAddress ();

    /**
     * Gets the list of supported source file name suffixes. The list is case
     * insensitive. This method is called only once by emuStudio.
     *
     * @return list of supported source file name suffixes
     */
    SourceFileExtension[] getSourceSuffixList();

}

