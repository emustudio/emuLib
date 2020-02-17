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

package net.emustudio.emulib.plugins;

import net.emustudio.emulib.runtime.EmuStudio;
import net.emustudio.emulib.runtime.PluginInitializationException;
import net.emustudio.emulib.runtime.PluginSettings;

/**
 * Plugin main interface. This interface is available only to emuStudio, not to other plugins. Plugins communicate
 * with each other using special entities called "contexts".
 * <p>
 * CONTRACT:
 * <p>
 * Each plugin must meet several requirements:
 * <p>
 * - this interface, or its derivative, must be implemented by one and only one class (so-called "root class")
 * - the root class has to be annotated with {@link net.emustudio.emulib.plugins.annotations.PluginRoot} annotation
 * with required parameters
 * - the root class has to have single constructor with two parameters.
 * <p>
 * The constructor signature must be as follows:
 *
 * <code>
 * \@PluginRoot(...)
 * SamplePlugin(Long pluginId, EmuStudio emustudio) {
 * ...
 * }
 * </code>
 * <p>
 * 1. pluginId is a unique plugin identification. Various emuLib operations (like reading plugin settings) require
 * pluginId as input argument.
 * <p>
 * 2. emuStudio is the main API used for communication with emuStudio and other plugins.
 * <p>
 * Plugins should initialize and register the contexts they provide into the {@link EmuStudio#getContextPool()} in the
 * constructor. {@link net.emustudio.emulib.runtime.ContextPool} is a container which contains contexts of all plugins.
 * <p>
 * Plugins must not try to obtain other context(s) from the contextPool in the constructor, because the order of context
 * registration is fixed. In order to obtain plugin contexts, {@link Plugin#initialize(PluginSettings)} should be used.
 */
public interface Plugin {

    /**
     * Reset plugin.
     * <p>
     * "Reset" means to bring the plugin to its "initial state", as it was after calling
     * {@link Plugin#initialize(PluginSettings)}.
     */
    void reset();

    /**
     * Initialize plugin.
     * <p>
     * This method is called just once, after emuStudio creates the new instance of the plugin. Here the plugin can
     * request already registered contexts from the {@link EmuStudio#getContextPool()}, or read its settings using
     * provided settingManager.
     *
     * @param pluginSettings manager of plugin's settings. plugin use it for
     *                       getting/storing/removing its custom settings. These settings are saved
     *                       directly into the configuration file.
     * @throws PluginInitializationException thrown when initialization process was not successful
     */
    void initialize(PluginSettings pluginSettings) throws PluginInitializationException;

    /**
     * Destroys all plugin resources.
     * <p>
     * This method is called immediately after user closes the emulator.
     * <p>
     * Inside the method, the plugin should:
     * - unregister all registered contexts for this plugin
     * - execute clean-up/destroy code for used resources (GUIs, timers, threads, sockets, memory, etc).
     */
    void destroy();

    /**
     * Show GUI of plugin settings, if it is provided.
     * <p>
     * Each plugin can have a nice GUI for settings manipulation. plugins should display the GUI, if they support it.
     * <p>
     * In the case of memory plugin, show GUI of a memory. Each memory plugin should have a GUI, but it is not a must.
     */
    void showSettings();

    /**
     * Check if showSettings() is supported by this plugin.
     *
     * @return true if the plugin has a GUI for settings; false otherwise
     */
    boolean isShowSettingsSupported();

    /**
     * Get run-time title of this plugin.
     * <p>
     * The title is a matter of change during runtime - for example there
     * might be a device which might be used many times but it is called
     * differently for each instance.
     *
     * @return run-time title of the plugin
     */
    String getTitle();

    /**
     * Get version of the plugin.
     *
     * @return version string
     */
    String getVersion();
}

