package plugins;


/**
 *  <html>
 *    <head>
 *  
 *    </head>
 *    <body>
 *      <p style="margin-top: 0">
 *        Root interface for all plugins, defines description, version, name and 
 *        copyright information.
 *      </p>
 *    </body>
 *  </html>
 */
// <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
// #[regen=yes,id=DCE.1226D954-1AF0-19DD-F109-46342AFBB4FB]
// </editor-fold> 
public interface IPlugin {

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.7A10F350-57E1-7468-C2E4-A06F336E461A]
    // </editor-fold>
    /**
     * Perform a reset of this plugin. Resetion process depends on kind of
     * the plugin.
     */
    public void reset ();

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.A1EFEA47-E792-C2B3-E23F-C6E34345E57C]
    // </editor-fold>
    /**
     * Get name of the plugin. This name is only "marketing"-name, it has
     * no relevance for identifying the plugin. This name is shown in "Preview
     * Configuration" in main module, and if the plugin is device, also in
     * "devices" section in panel "emulator" in the main module.
     * @return name of the plugin
     */
    public String getName ();

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.AE1ED855-A632-3E3C-686B-59A2D23AD51C]
    // </editor-fold> 
    /**
     * Get legal copyright of the plugin.
     * @return legal copyright
     */
    public String getCopyright ();

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.D3D83981-7394-3D18-32BE-5682CE111890]
    // </editor-fold> 
    /**
     * Get description of the plugin. It should not be used as a manual :-),
     * it should be short.
     * @return short description of the plugin
     */
    public String getDescription ();

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.BF02C7C2-CD2A-E2B7-5094-5A003A6C1EF2]
    // </editor-fold>
    /**
     * Get version of the plugin. String is returned, so version can be in
     * arbitrary format. This version should not to be used for identifying
     * the plugin. Better for this purpose is checking plugin's context version
     * and ID.
     * @return version of the plugin
     */
    public String getVersion ();

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.9DBC73F7-ABE4-8D0F-25B7-CF3FC79DE90E]
    // </editor-fold> 
    /**
     * This method is called immediately after user closes the emulator. It means,
     * that after return from this method instance of the plugin will be destroyed.
     * It should contain some clean-up or destroy code for GUIs, stop timers,
     * threads, etc. 
     */
    public void destroy ();

}

