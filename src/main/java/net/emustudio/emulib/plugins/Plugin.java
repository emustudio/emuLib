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

import net.emustudio.emulib.runtime.ApplicationApi;

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
 * - the root class has to have single constructor with 3 parameters (see below).
 * <p>
 * The constructor signature has to look like this:
 *
 * <code>
 * \@PluginRoot(...)
 * SamplePlugin(long pluginId, ApplicationApi emustudio, PluginSettings settings) {
 * ...
 * }
 * </code>
 * <p>
 * 1. <code>pluginId</code> is a unique plugin identification. Operations around plugin contexts require it as input argument.
 * <p>
 * 2. <code>emustudio</code> is the API provided by emuStudio application, to be used by plugins.
 * <p>
 * 3. <code>settings</code> plugin's settings. A plugin can use it for reading/writing its custom settings. Updated settings
 * are saved immediately in the configuration file, in the same thread.
 * <p>
 * NOTE: Plugins should register the contexts they provide into the {@link net.emustudio.emulib.runtime.ContextPool},
 * which can be obtained from {@link ApplicationApi} API.
 * <p>
 * Plugins must not try to obtain contexts from the context pool from plugin constructor. The reason is that the order
 * of plugin context registration is fixed, and in the plugin construction time some contexts don't need to be available
 * yet. Plugin contexts can be safely obtained within {@link Plugin#initialize()} method.
 */
@SuppressWarnings("unused")
public interface Plugin {

    /**
     * Reset plugin.
     * <p>
     * "Reset" means to bring the plugin to its "initial state", as it was after calling {@link Plugin#initialize()}.
     */
    void reset();

    /**
     * Initialize plugin.
     * <p>
     * This method is called just once, after emuStudio creates the new instance of the plugin. Here the plugin can
     * request already registered contexts from the {@link ApplicationApi#getContextPool()}, or read its settings using
     * provided settingManager.
     *
     * @throws PluginInitializationException thrown when initialization process was not successful
     */
    void initialize() throws PluginInitializationException;

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

    /**
     * Get copyright string.
     *
     * @return copyright string
     */
    String getCopyright();

    /**
     * Get plugin short description.
     *
     * @return plugin description
     */
    String getDescription();
}

