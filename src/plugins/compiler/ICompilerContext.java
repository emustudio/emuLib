/**
 * ICompilerContext.java
 *
 * (c) Copyright 2010, P. Jakubčo <pjakubco@gmail.com>
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
 */
package plugins.compiler;

import java.util.EventListener;
import java.util.EventObject;
import plugins.IContext;

/**
 */
public interface ICompilerContext extends IContext {
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


    public void addCPUListener (ICompilerListener listener);
    public void removeCPUListener (ICompilerListener listener);

    public interface ICompilerListener extends EventListener {
        public void onCompileStart (EventObject evt);

        /**
         * Method will fire when compiler wants to print something on screen.
         * The message is usually error report, but can have information
         * character.
         *
         * @param evt The event object
         * @param line
         *        Line in the source code. When it is -1, then it should
         *        NOT to be considered.
         * @param row
         *        Row in the source code. When it is -1, then it should
         *        NOT to be considered.
         * @param message
         *        Message from the compiler
         * @param errorCode
         *        Error code (when it is an error report)
         * @param messageType
         *        Type of the message. One of the TYPE_WARNING, TYPE_ERROR,
         *        or TYPE_INFO.
         */
        public void onCompileInfo (EventObject evt, int line, int row,
                String message, int errorCode, int messageType);
        public void onCompileFinish (EventObject evt, int errorCode);
    }

}

