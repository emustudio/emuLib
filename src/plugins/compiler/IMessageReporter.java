/**
 * IMessageReporter.java
 * 
 * (c) Copyright 2008-2009, P.Jakubčo <pjakubco@gmail.com>
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
 * Interface for reporting messages while running compilation process. It is
 * used for sending compiling messages to main module, e.g. warnings, errors, etc.
 * These messages are showed in bottom text area in panel "source code" in the
 * main module.
 */
public interface IMessageReporter {

	public static final int TYPE_WARNING = 1;
	public static final int TYPE_ERROR   = 2;
	public static final int TYPE_INFO    = 3;
	
    /**
     * Method reports some message to a main module.
     * @param message   message to report
     * @param type      type of the message (one of the
     *                  <code>TYPE_WARNING</code>, <code>TYPE_ERROR</code>,
     *                  <code>TYPE_INFO</code>)
     */
    public void report (String message, int type);

    /**
     * Method reports some message to a main module with location information.
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

