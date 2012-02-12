/**
 * IPlugin.java
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

package emulib.plugins;


/**
 * Root interface for all plugins, defines description, version, name and 
 * copyright information.
 *
 * Within the plug-in constructor, it gets the identification number
 * (pluginID). The plug-in should keep this ID for future usage within the
 * communication with the emuLib.
 *
 * In the constructor, the plug-in should register all implementing contexts
 * through runtime.Context.register() method.
 * 
 */

public interface IPlugin {
	
    /**
     * Perform a reset of this plugin. Reset process depends on the type of
     * the plugin.
     */
    public void reset ();

    /**
     * Get name of the plugin. This name is only "marketing"-name, it has
     * no relevance for identifying the plugin. This name is shown in "Preview
     * Configuration" in main module, and if the plugin is device, also in
     * "devices" section in panel "emulator" in the main module.
     * @return name of the plugin
     */
    public String getTitle ();

    /**
     * Get legal copyright of the plugin.
     * @return legal copyright
     */
    public String getCopyright ();

    /**
     * Get short description of the plugin. It should not be used as a manual :-)
     * 
     * @return short description of the plugin
     */
    public String getDescription ();

    /**
     * Get version of the plugin. String is returned, so version can be in
     * arbitrary format. This version should not to be used for identifying
     * the plugin. Better for this purpose is checking plugin's context version
     * and ID.
     * @return version of the plugin
     */
    public String getVersion ();

    /**
     * Perform initialization process of this plug-in. It is called by the main
     * module.
     *
     * Within this method, all required contexts should be gained through
     * the runtime.Context class.
     * 
     * @param sHandler  settings handler object. Device can use this for
     *                  accessing/storing/removing its settings.
     * @return true if initialization process was successful
     */
    public boolean initialize (ISettingsHandler sHandler);

    /**
     * This method is called immediately after user closes the emulator. It means,
     * that after return from this method instance of the plugin will be destroyed.
     * It should contain some clean-up or destroy code for GUIs, stop timers,
     * threads, etc.
     *
     * The other thing that should be done here is to unregister all registered
     * contexts for this plug-in.
     */
    public void destroy ();
    
    /**
     * Each plug-in can own a GUI for settings manipulation. This
     * method invokes it.
     *
     * In the case of memory plug-in, show GUI of a memory. Each memory plugin
     * should have a GUI, but it is not necessary.
     */
    public void showSettings ();
    
    /**
     * Check whether settings GUI is supported by this plug-in.
     * @return true if settings GUI is supported, false otherwise
     */
    public boolean isShowSettingsSupported();

}

