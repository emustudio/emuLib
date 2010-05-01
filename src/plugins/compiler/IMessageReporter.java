/**
 * IMessageReporter.java
 * 
 * (c) Copyright 2008-2010, P.Jakubčo <pjakubco@gmail.com>
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

/**
 * Interface for reporting messages. Object implementing the interface is used
 * by the compiler, during the compilation process. It realizes a "bridge"
 * between the main module and a compiler. The compiler uses it for sending
 * compile messages that should be shown in the main module (warnings,
 * errors, informations).
 *
 * In the main module, these messages are shown at the bottom, in the panel
 * "source code".
 */
public interface IMessageReporter {

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
     * Method for reporting a message.
     *
     * @param message   message to report
     * @param type      type of the message (one of the
     *                  <code>TYPE_WARNING</code>, <code>TYPE_ERROR</code>,
     *                  <code>TYPE_INFO</code>)
     */
    public void report (String message, int type);

    /**
     * Method for reporting a message including the location (row, column in the
     * source code) information.
     * 
     * @param row       row in the source code that is related to the message
     * @param column    column in the source code that is related to the message
     * @param message   message to report
     * @param type      type of the message (one of the
     *                  <code>TYPE_WARNING</code>, <code>TYPE_ERROR</code>,
     *                  <code>TYPE_INFO</code>)
     */
    public void report (int row, int column, String message, int type);

}

