// SPDX-License-Identifier: GPL-3.0-or-later
package net.emustudio.emulib.runtime.interaction.debugger;


/**
 * Column in the debugger table.
 *
 * @see DebuggerTable
 * @param <T> Java type of this column
 */
@SuppressWarnings("unused")
public interface DebuggerColumn<T> {

    /**
     * Gets class type of the column.
     *
     * @return Java type of this column
     */
    Class<T> getClassType ();

    /**
     * Gets title of the column.
     *
     * @return title of this column
     */
    String getTitle ();

    /**
     * Determines whether this column is editable by user.
     *
     * For example, a "mnemo" column shouldn't be editable, but "breakpoint" column can (and should).
     * Note that the returned value holds to all cells in the column.
     *
     * @return true if the column is editable, false otherwise
     */
    boolean isEditable ();

    /**
     * Set a value to a column cell.
     *
     * It is called by emuStudio when user sets a value to a cell in the debugger table. It is used only if
     * the column is editable (see {@link #isEditable() isEditable} method).
     *
     * NOTE: This method should ensure proper changes in CPU's internal state, if necessary.
     *
     * @param location Cell index (usually memory address). It is not a "row number" in the debugger table.
     * @param value new value assigned to the cell
     * @throws CannotSetDebuggerValueException if the value cannot be assigned, for any reason
     */
    void setValue(int location, Object value) throws CannotSetDebuggerValueException;

    /**
     * Gets a value from the column cell.
     *
     * It is called by emuStudio when rendering the debugger table.
     *
     * @param location Cell index (usually memory address). It is not a "row number" in the debugger table.
     * @return cell value
     */
    T getValue(int location);

    /**
     * Get default width of the column in pixels.
     *
     * If it is not known, this method should return -1.
     *
     * @return default width of the column in pixels or -1 if it is not known
     */
    default int getDefaultWidth() {
        return -1;
    }
}
