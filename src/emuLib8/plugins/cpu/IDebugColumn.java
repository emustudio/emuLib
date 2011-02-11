/**
 * IDebugColumn.java
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
package emuLib8.plugins.cpu;


/**
 * Interface that holds information about column in debug window.
 */
public interface IDebugColumn {

    /**
     * Gets class type of the column. Mostly the column type is <code>java.lang.String</code>,
     * but for breakpoint columns should be used <code>java.lang.Boolean</code>
     * class.
     * @return Java type of this column
     */
    public Class getClassType ();

    /**
     * Gets name (title) of the column.
     * @return title of this column
     */
    public String getTitle ();

    /**
     * Determines whether this column is editable by user. For example, mnemonics
     * column shouldn't be editable (if CPU doesn't support assembly in runtime),
     * but breakpoint cells should. If the column is editable, main module after
     * editing the corresponding cell invokes <code>ICPU.setDebugValue</code>
     * method and this method should take care of internal change in CPU.
     * @return true if column (with all its cells) is editable, false otherwise
     */
    public boolean isEditable ();

    /**
     * Called when user sets a value to a cell in debug window. This method
     * should ensure proper changes in CPU's internal state, caused by this
     * set. The main module calls this method only if the cell in debug window
     * is editable by user (<code>IDebugColumn.isEditable()</code>).
     * @param row    cell's index from memory position 0 (not row in debug table)
     * @param value  new value of the cell
     */
    public void setDebugValue (int row, Object value);

    /**
     * Gets the value of a cell in debug window at specified position.
     * @param row  cell's index from memory position 0 (not row in debug table)
     * @return value of the cell
     */
    public Object getDebugValue (int row);

    /**
     * Determine whether the cell in the specified row is an actual instruction.
     * This is needed by emuStudio when it needs to color the row in the debug
     * table. Current instruction is painted with another color than other rows.
     *
     * @param row The memory address
     * @return true if the instruction at specified location is current instruction,
     * false otherwise
     */
    public boolean isCurrent(int row);

    /**
     * Returns index value of row in the debug table of current instruction.
     *
     * @return Row index in debug table of current instruction
     */
    public int getCurrentDebugRow();

}

