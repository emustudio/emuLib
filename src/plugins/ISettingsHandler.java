package plugins;

/**
 * Interface for all plugins, it perform methods for reading/saving settings.
 * It is implemented by the main module and plugins obtain its object by an
 * parameter in initialization proces.
 */
public interface ISettingsHandler {

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

    /**
     * Enumeration for available types of the plugin
     */
    public enum pluginType {

        /**
         * Plugin is a CPU
         */
        cpu,

        /**
         * Plugin is a device
         */
        device,

        /**
         * Plugin is a memory
         */
        memory,

        /**
         * Plugin is a compiler
         */
        compiler;


    }

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

