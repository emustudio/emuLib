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
package plugins.compiler;

import java.io.Reader; 
import plugins.memory.IMemoryContext;
import plugins.IPlugin; 
import plugins.ISettingsHandler; 

/**
 * This interface is the core for compiler plugin types. These plugins
 * should implement this interface once and only once.
 */
public interface ICompiler extends IPlugin {

    /**
     * Perform initialization process of a compiler. This method is called
     * immediately after plugins are loaded into memory.
     * @param sHandler settings handler object. Compiler can use this for
     *                 accessing/storing/removing its settings.
     * @param reporter  object for reporting messages (warnings, errors, ...). 
     *                  This object is implemented in main module and is connected
     *                  to text area in panel "source code" in the main module.
     *                  Plugin should use this.
     *
     * @return true if initialization was successful, false otherwise 
     */
    public boolean initialize (ISettingsHandler sHandler, IMessageReporter reporter);

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
     * Compile a file into output file and into an operating memory. Output file
     * name the compiler should derive from input file name.
     * @param fileName  name of input file (source code)
     * @param in        <code>Reader</code> object of the document - source code.
     * @param mem       memory context object - it is used if compiler compiles
     *                  the source into memory. Compiler should check this
     *                  parameter for <code>null</code>.
     *                  
     * @return true if compile was successful, false otherwise
     */
    public boolean compile (String fileName, Reader in, IMemoryContext mem);

    /**
     * Get a lexical analyzer of the compiler. It is used by main module for
     * syntax highlighting and of course in compile process by the compiler.
     * For every call it should return new object (instance).
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

