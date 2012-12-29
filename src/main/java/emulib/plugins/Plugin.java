/*
 * Plugin.java
 * 
 * KISS, YAGNI, DRY
 * 
 * (c) Copyright 2008-2012, Peter Jakubčo
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

/**
 * Main interface for plug-ins implementation.
 * 
 * Each plug-in must meet several requirements:
 *   - it has to implement single class that implements this (or derived) interface (main class)
 *   - the main class has to have @PluginType annotation with parameters as title, copyright, etc.
 *   - the main class has to have single constructor with single parameter of Long type.
 * 
 * Within the constructor, the plug-in receives the identification number (pluginID). The plug-in should keep this ID
 * for future communication with emuLib.
 *
 * Context initialization and registration should be made inside initialize method.
 * 
 * Each method inside this interface can be called only by emuStudio. Other plug-ins cannot get plug-in objects, they
 * communicate through contexts only.
 * 
 */

public interface Plugin {
	
    /**
     * Perform a reset of this plugin.
     * 
     * Reset process depends on the type of the plugin and it should bring it to some default initial state.
     */
    public void reset ();

    /**
     * Perform initialization of the plug-in.
     * 
     * Within this method, all provided contexts should be registered, or all required contexts requested from emuLib.
     * 
     * @param settingsManager  manager of plug-in's settings. Plug-in use it for getting/storing/removing
     *        its custom settings. These settings are saved directly into the configuration file.
     * @return true if initialization process was successful; false otherwise
     */
    public boolean initialize (SettingsManager settingsManager);

    /**
     * Destroys all plug-in resources.
     * 
     * This method is called immediately after user closes the emulator.
     * 
     * Inside the method, the plug-in should:
     *   - unregister all registered contexts for this plug-in
     *   - execute clean-up/destroy code for used resources (GUIs, timers, threads, sockets, memory, etc).
     */
    public void destroy ();
    
    /**
     * Show GUI of plug-in settings, if it is provided.
     * 
     * Each plug-in can have a nice GUI for settings manipulation. Plug-ins should display the GUI, if they support it.
     *
     * In the case of memory plug-in, show GUI of a memory. Each memory plugin should have a GUI, but it is not a must.
     */
    public void showSettings ();
    
    /**
     * Check if showSettings() is supported by this plug-in.
     * @return true if the plug-in has a GUI for settings; false otherwise
     */
    public boolean isShowSettingsSupported();
    
    /**
     * Get run-time title of this plug-in.
     * 
     * The title is a matter of change during runtime - for example there
     * might be a device which might be used many times but it is called
     * differently for each instance.
     * 
     * @return run-time title of the plug-in
     */
    public String getTitle();
    
    /**
     * Get version of the plug-in.
     * 
     * @return version string
     */
    public String getVersion();

}

