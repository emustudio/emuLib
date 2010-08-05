/**
 * ISettingsHandler.java
 * 
 * (c) Copyright 2008-2010, P. Jakubčo <pjakubco@gmail.com>
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
     * @param pluginID  plugin ID
     * @param settingName  name of wanted setting (without spaces)
     * @return setting if it exists (as a <code>String</code>), or 
     *         <code>null</code> if not
     */
    public String readSetting (long pluginID, String settingName);

    /**
     * Write specified setting to a configuration file. Setting can be arbitrary. It 
     * uses configuration file that user has chosen after start of the emulator.
     * @param pluginID  plugin ID
     * @param settingName  name of wanted setting (without spaces) to be written
     * @param val          value of the setting (has to be <code>String</code> type)
     */
    public void writeSetting (long pluginID, String settingName, String val);

    /**
     * Remove specified setting to from a configuration file. Be careful, setting can 
     * be arbitrary. It uses configuration file that user chosen after start of the
     * emulator.
     * @param pluginID  plugin ID
     * @param settingName  name of wanted setting (without spaces) to be removed
     */
    public void removeSetting (long pluginID, String settingName);

}

