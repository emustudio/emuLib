/**
 * ISettingsHandler.java
 * 
 * (c) Copyright 2008-2009, P. Jakubčo
 * 
 * KISS, YAGNI
 * 
 */

package plugins;

/**
 * Interface for all plugins, it perform methods for reading/storing settings.
 * It is implemented by the main module and plugins obtain its object by an
 * parameter in initialization process.
 */
public interface ISettingsHandler {

    /**
     * Read specified setting from configuration file.  Setting can be arbitrary. It 
     * uses configuration file that user chosen after start of the emulator.
     * 
     * @param pluginHash   hash of a plugin
     * @param settingName  name of wanted setting (without spaces)
     * @return setting if it exists (as a <code>String</code>), or 
     *         <code>null</code> if not
     */
    public String readSetting (String pluginHash, String settingName);

    /**
     * Write specified setting to a configuration file. Setting can be arbitrary. It 
     * uses configuration file that user has chosen after start of the emulator.
     * @param pluginHash   hash of a plugin
     * @param settingName  name of wanted setting (without spaces) to be written
     * @param val          value of the setting (has to be <code>String</code> type)
     */
    public void writeSetting (String pluginHash, String settingName, String val);

    /**
     * Remove specified setting to from a configuration file. Be careful, setting can 
     * be arbitrary. It uses configuration file that user chosen after start of the
     * emulator.
     * @param pluginHash   hash of a plugin
     * @param settingName  name of wanted setting (without spaces) to be removed
     */
    public void removeSetting (String pluginHash, String settingName);

}

