/**
 * ICompiler.java
 * 
 * (c) Copyright 2008-2010 P. Jakubčo <pjakubco@gmail.com>
 * 
 * KISS, YAGNI
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 */
package emuLib8.plugins.compiler;

import java.io.Reader; 
import java.util.EventListener;
import java.util.EventObject;
import emuLib8.plugins.IPlugin;

/**
 * This interface is the core for compiler plugin types. These plugins
 * should implement this interface once and only once.
 */
public interface ICompiler extends IPlugin {
    /**
     * The message passed from compiler is a warning.
     */
    public static final int TYPE_WARNING = 1;
    /**
     * The message passed from compiler is an error.
     */
    public static final int TYPE_ERROR   = 2;
    /**
     * The message passed from compiler is an information.
     */
    public static final int TYPE_INFO    = 3;

    /**
     * Adds ICompilerListener object into list of listeners.
     * If some event is occured in the compiler, listeners methods will be
     * called.
     *
     * @param listener ICompilerListener object
     */
    public void addCompilerListener (ICompilerListener listener);

    /**
     * Removes ICompilerListener object from the listeners list.
     *
     * @param listener ICompilerListener object
     */
    public void removeCompilerListener (ICompilerListener listener);

    /**
     * This is an interface that should be implemented by the emuStudio,
     * and/or other plug-ins that want to process the output of the compiler.
     */
    public interface ICompilerListener extends EventListener {
        /**
         * This method is called whenever a compiler begins to work.
         *
         * @param evt Event object
         */
        public void onCompileStart (EventObject evt);

        /**
         * Method will fire when compiler wants to print something on screen.
         * The message is usually error report, but can have information
         * character.
         *
         * @param evt The event object
         * @param row
         *        Row in the source code. When it is -1, then it should
         *        be IGNORED.
         * @param col
         *        Column in the source code. When it is -1, then it should
         *        be IGNORED.
         * @param message
         *        Message from the compiler
         * @param errorCode
         *        Error code (when it is an error report)
         * @param messageType
         *        Type of the message. One of the TYPE_WARNING, TYPE_ERROR,
         *        or TYPE_INFO.
         */
        public void onCompileInfo (EventObject evt, int row, int col,
                String message, int errorCode, int messageType);

        /**
         * This method is called whenever the compiler finishes the compile
         * job.
         *
         * @param evt Event object
         * @param errorCode compiler-specific error code
         */
        public void onCompileFinish (EventObject evt, int errorCode);
    }

    /**
     * Compile a file into output file. Output file name the compiler should 
     * derive from input file name.
     * @param fileName  name of input file (source code)
     * @param in        <code>Reader</code> object of the document - source code.
     *
     * @return true if compile was successful, false otherwise
     */
    public boolean compile (String fileName, Reader in);
    
    /**
     * Get a lexical analyzer of the compiler. It is used by main module for
     * syntax highlighting. In the compilation process the compiler should
     * use own, independent lexical analyzer.
     *
     * @param in  <code>Reader</code> object of the document - source code.
     * @return lexical analyzer object
     */
    public ILexer getLexer(Reader in);
    
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
    public int getProgramStartAddress ();

}

