/*
 * Run-time library for emuStudio and plug-ins.
 *
 *     Copyright (C) 2006-2020  Peter Jakubčo
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.emustudio.emulib.runtime;

import net.emustudio.emulib.plugins.Plugin;

import java.util.Optional;

/**
 * The class provides methods for reading/writing plugin settings.
 * <p>
 * It is implemented by the emuStudio. Plugins get it within the {@link Plugin#initialize(PluginSettings)}
 * method.
 * <p>
 * A setting is a <code>(key, value)</code> pair. Keys and values are Strings.
 * <p>
 * Key format must be a single word, without spaces. Plugin keys must not start with <code>"emustudio_"</code> prefix.
 * This prefix is reserved for emuStudio settings and might be overridden by emuStudio.
 */
@SuppressWarnings("unused")
public interface PluginSettings {

    /**
     * Setting key defining if emuStudio runs in "No GUI" mode.
     */
    String EMUSTUDIO_NO_GUI = "emustudio_nogui";

    /**
     * Setting key defining if emuStudio runs in "Auto" (non-interactive) mode.
     */
    String EMUSTUDIO_AUTO = "emustudio_auto";

    /**
     * Read a plugin setting.
     *
     * @param pluginID plugin ID
     * @param key      name of the setting
     * @return setting value if it exists (as a <code>String</code>), or
     * <code>null</code> if not
     */
    Optional<String> read(long pluginID, String key);

    /**
     * Write a plugin setting.
     *
     * @param pluginID plugin ID
     * @param key      name of the setting
     * @param value    new value of the setting
     * @throws CannotWriteSettingException if the setting could not be written (e.g. key is in wrong format, etc.)
     */
    void write(long pluginID, String key, String value) throws CannotWriteSettingException;

    /**
     * Remove a plugin setting.
     *
     * @param pluginID    plugin ID
     * @param settingName name of wanted setting (without spaces) to be removed
     * @throws CannotWriteSettingException if the setting could not be written (e.g. key is in wrong format, etc.)
     */
    void remove(long pluginID, String settingName) throws CannotWriteSettingException;
}
