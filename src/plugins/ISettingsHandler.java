package plugins;

/**
 * Interface for all plugins, it perform methods for reading/saving settings.
 * It is implemented by the main module and plugins obtain its object by an
 * parameter in initialization proces.
 */
// <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
// #[regen=yes,id=DCE.94F44BE0-1CDB-788B-A067-19533BAE992D]
// </editor-fold> 
public interface ISettingsHandler {

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.A67E563F-AF27-768D-2CB9-2597911899C2]
    // </editor-fold>
    /**
     * Read specified setting from config file.  Setting can be arbitrary. It 
     * uses config file that user chosed after start of the emulator.
     * @param plType       plugin type
     * @param pluginID     this parameter is relevant only for device plugins. It is
     *                     a filename of the device, without extension (e.g.
     *                     if 'terminal.jar' is a filename of a device, then 
     *                     'terminal' should be used as the parameter)
     * @param settingName  name of wanted setting (without spaces)
     * @return setting if it exists (as a <code>String</code>), or 
     *         <code>null</code> if not
     */
    public String readSetting (ISettingsHandler.pluginType plType, String pluginID, String settingName);

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.EFE21772-F086-BAB6-D6F2-305FCB88CC77]
    // </editor-fold> 
    /**
     * Write specified setting to a config file. Setting can be arbitrary. It 
     * uses config file that user chosed after start of the emulator.
     * @param plType       plugin type
     * @param pluginID     this parameter is relevant only for device plugins. It is
     *                     a filename of the device, without extension (e.g.
     *                     if 'terminal.jar' is a filename of a device, then 
     *                     'terminal' should be used as the parameter)
     * @param settingName  name of wanted setting (without spaces) to be written
     * @param val          value of the setting (has to be <code>String</code> type)
     */
    public void writeSetting (ISettingsHandler.pluginType plType, String pluginID, String settingName, String val);

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.8F6397FB-4B34-C8F0-043B-968363A418DD]
    // </editor-fold> 
    /**
     * Enumeration for available types of the plugin
     */
    public enum pluginType {

        // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
        // #[regen=yes,id=DCE.797DA4B5-07B3-B372-5D33-092EF413CE19]
        // </editor-fold> 
        /**
         * Plugin is a CPU
         */
        cpu,

        // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
        // #[regen=yes,id=DCE.A557D7F2-E442-145D-47EA-75F7D2C8295E]
        // </editor-fold> 
        /**
         * Plugin is a device
         */
        device,

        // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
        // #[regen=yes,id=DCE.032A8553-E234-3C6A-5E28-FBF2E6A924CB]
        // </editor-fold> 
        /**
         * Plugin is a memory
         */
        memory,

        // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
        // #[regen=yes,id=DCE.A76F0131-5F39-9017-6436-3A44F9951144]
        // </editor-fold> 
        /**
         * Plugin is a compiler
         */
        compiler;


    }

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.854772DA-03CA-899B-7E02-2F554DA49B15]
    // </editor-fold> 
    /**
     * Remove specified setting to from a config file. Be careful, setting can 
     * be arbitrary. It uses config file that user chosed after start of the
     * emulator.
     * @param plType       plugin type
     * @param pluginID     this parameter is relevant only for device plugins. It is
     *                     a filename of the device, without extension (e.g.
     *                     if 'terminal.jar' is a filename of a device, then 
     *                     'terminal' should be used as the parameter)
     * @param settingName  name of wanted setting (without spaces) to be removed
     */
    public void removeSetting (ISettingsHandler.pluginType plType, String pluginID, String settingName);

}

