package plugins;


// <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
// #[regen=yes,id=DCE.90438BB4-33AB-374D-220E-9896C5E51DF1]
// </editor-fold> 
public interface IContext {

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.C93D057C-0E99-F84E-7538-FE79453A553E]
    // </editor-fold>
    /**
     * Return unique ID of this context. This can be anything. Usually it
     * is related with kind of context. Other plugins can identify the context
     * by recognization of its ID.
     * @return ID of this context.
     */
    public String getID ();

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.48085642-FD63-63E4-08FF-56AB19C89A12]
    // </editor-fold> 
    /**
     * Get major version number (e.g. for version 3.2b1 it should return 3)
     * @return major version number
     */
    public int getVersionMajor ();

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.C045BBD9-1652-23BF-4173-F0072058F4FF]
    // </editor-fold> 
    /**
     * Get minor version number (e.g. for version 3.2b1 it should return 2)
     * @return minor version number
     */
    public int getVersionMinor ();

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.A10F2AD8-BDD8-60A5-60E7-AF1527BE8F1E]
    // </editor-fold> 
    /**
     * Get revision number (e.g. for version 3.2b1 it should return b1). Revision
     * can be any String.
     * @return revision number
     */
    public String getVersionRev ();

}

