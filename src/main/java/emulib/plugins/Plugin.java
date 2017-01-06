/*
 * KISS, YAGNI, DRY
 *
 * (c) Copyright 2006-2017, Peter Jakubčo
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

package emulib.plugins;

import emulib.emustudio.SettingsManager;
import emulib.runtime.exceptions.PluginInitializationException;

/**
 * Plug-in main interface. The interface methods are available only to emuStudio. Plug-ins communicate between
 * each other using special entities called "contexts".
 *
 * CONTRACT:
 *
 * Each plug-in must meet several requirements:
 *
 *   - this interface, or its derivate, must be implemented by one and only one class (so-called "main-class")
 *   - the main class has to be annotated with @PluginType annotation with required parameters
 *   - the main class has to have single constructor with two parameters.
 *
 * The constructor signature must be as follows:
 *
 *     SamplePlugin(Long pluginId, ContextPool contextPool) {
 *         ...
 *     }
 *
 * 1. pluginId is a unique plug-in identification. Various emuLib operations (like reading plug-in settings) require
 *    pluginId as input argument.
 *
 * 2. contextPool represents a pool of communication entities called "contexts". Contexts represent plug-in API intended
 *    for the communication with other plug-ins.
 *
 * Plug-ins should initialize and register the contexts they provide into the contextPool within the plug-in constructor.
 *
 * Plug-ins must not try to obtain other context from the contextPool within the constructor. For this purpose there
 * exists the initialize() method.
 *
 */

public interface Plugin {

    /**
     * Perform a reset of this plugin.
     *
     * Reset process depends on the type of the plugin and it should bring it to some default initial state.
     */
    void reset ();

    /**
     * Perform initialization of the plug-in.
     *
     * Within this method, all provided contexts should be registered, or all
     * required contexts requested from emuLib.
     *
     * @param settingsManager  manager of plug-in's settings. Plug-in use it for
     *   getting/storing/removing its custom settings. These settings are saved
     *   directly into the configuration file.
     * @throws PluginInitializationException
     * thrown when initialization process was not successful

     */
    void initialize (SettingsManager settingsManager) throws PluginInitializationException;

    /**
     * Destroys all plug-in resources.
     *
     * This method is called immediately after user closes the emulator.
     *
     * Inside the method, the plug-in should:
     *   - unregister all registered contexts for this plug-in
     *   - execute clean-up/destroy code for used resources (GUIs, timers, threads, sockets, memory, etc).
     */
    void destroy ();

    /**
     * Show GUI of plug-in settings, if it is provided.
     *
     * Each plug-in can have a nice GUI for settings manipulation. Plug-ins should display the GUI, if they support it.
     *
     * In the case of memory plug-in, show GUI of a memory. Each memory plugin should have a GUI, but it is not a must.
     */
    void showSettings ();

    /**
     * Check if showSettings() is supported by this plug-in.
     * @return true if the plug-in has a GUI for settings; false otherwise
     */
    boolean isShowSettingsSupported();

    /**
     * Get run-time title of this plug-in.
     *
     * The title is a matter of change during runtime - for example there
     * might be a device which might be used many times but it is called
     * differently for each instance.
     *
     * @return run-time title of the plug-in
     */
    String getTitle();

    /**
     * Get version of the plug-in.
     *
     * @return version string
     */
    String getVersion();

}

