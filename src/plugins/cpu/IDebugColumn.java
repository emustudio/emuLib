/**
 * IDebugColumn.java
 * 
 * (c) Copyright 2008-2009, P.Jakubčo
 * 
 * KISS, YAGNI
 * 
 */
package plugins.cpu;


/**
 * Interface that holds information about column in debug window.
 */
public interface IDebugColumn {

    /**
     * Gets java type of the column. Mostly the column type is <code>java.lang.String</code>,
     * but for breakpoint columns should be used <code>java.lang.Boolean</code>
     * class.
     * @return Java type of this column
     */
    public Class getType ();

    /**
     * Gets name (title) of the column.
     * @return title of this column
     */
    public String getName ();

    /**
     * Determines whether this column is editable by user. For example, mnemonics
     * column shouldn't be editable (if CPU doesn't support assembly in runtime),
     * but breakpoint cells should. If the column is editable, main module after
     * editing the corresponding cell invokes <code>ICPU.setDebugValue</code>
     * method and this method should take care of internal change in CPU.
     * @return true if column (with all its cells) is editable, false otherwise
     */
    public boolean isEditable ();

}

