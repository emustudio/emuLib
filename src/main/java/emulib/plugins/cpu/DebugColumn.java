/*
 * KISS, YAGNI, DRY
 *
 * (c) Copyright 2006-2016, Peter Jakubčo
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
package emulib.plugins.cpu;


/**
 * Interface that holds information about column in debug window.
 */
public interface DebugColumn {

    /**
     * Gets class type of the column. Mostly the column type is <code>java.lang.String</code>,
     * but for breakpoint columns should be used <code>java.lang.Boolean</code>
     * class.
     * @return Java type of this column
     */
    Class getClassType ();

    /**
     * Gets name (title) of the column.
     * @return title of this column
     */
    String getTitle ();

    /**
     * Determines whether this column is editable by user. For example, mnemonics
     * column shouldn't be editable (if CPU doesn't support assembly in runtime),
     * but breakpoint cells should.
     *
     * @return true if column (with all its cells) is editable, false otherwise
     */
    boolean isEditable ();

    /**
     * Called when user sets a value to a cell in debug window. This method
     * should ensure proper changes in CPU's internal state, caused by this
     * set. The main module calls this method only if the cell in debug window
     * is editable by user (<code>DebugColumn.isEditable()</code>).
     * @param location memory address (not row in debug table)
     * @param value  new value of the cell
     */
    void setDebugValue (int location, Object value);

    /**
     * Gets the value of a cell in debug window at specified position.
     * @param location  memory address (not row in debug table)
     * @return value of the cell
     */
    Object getDebugValue (int location);

    /**
     * Get default width of the column.
     *
     * If it is not known, this method should return -1.
     *
     * @return default width of the column or -1 if it is not known
     */
    default int getDefaultWidth() {
        return -1;
    }

}

