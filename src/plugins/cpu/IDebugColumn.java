package plugins.cpu;


// <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
// #[regen=yes,id=DCE.E68A4167-F79E-50CE-0352-A247DD47ADD5]
// </editor-fold> 
/**
 * Interface that holds information about column in debug window.
 */
public interface IDebugColumn {

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.D0532C73-56CD-F50D-53A0-1BD3E43F7D59]
    // </editor-fold>
    /**
     * Gets java type of the column. Mostly the column type is <code>java.lang.String</code>,
     * but for breakpoint columns should be used <code>java.lang.Boolean</code>
     * class.
     * @return Java type of this column
     */
    public Class getType ();

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.9DE2F163-518E-731E-BBA4-CEDBBDF14227]
    // </editor-fold> 
    /**
     * Gets name (title) of the column.
     * @return title of this column
     */
    public String getName ();

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.4212068E-5FE8-C678-9348-36F643FA3062]
    // </editor-fold> 
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

