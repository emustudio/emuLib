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

package plugins;


/**
 * Root interface for all plugins, defines description, version, name and 
 * copyright information.
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
     * This method is called immediately after user closes the emulator. It means,
     * that after return from this method instance of the plugin will be destroyed.
     * It should contain some clean-up or destroy code for GUIs, stop timers,
     * threads, etc. 
     */
    public void destroy ();
    
    /**
     * Every plugin should have its own GUI for settings manipulation. This 
     * method invokes it.
     */
    public void showSettings ();

    /**
     * The "hash" will be assigned to a plugin in their run-time, at once by its
     * constructor. The plugins will identify themselves using given hash.
     * 
     * @return hash that was assigned to the plugin
     */
    public long getHash ();
    
}

