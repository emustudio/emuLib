/**
 * IPlugin.java
 * 
 * (c) Copyright 2008-2009, P. Jakubčo
 * 
 * KISS, YAGNI
 */

package plugins;


/**
 * Root interface for all plugins, defines description, version, name and 
 * copyright information.
 * 
 */

public interface IPlugin {

    public static final int pluginCPU = 1;
	public static final int pluginCompiler = 2;
	public static final int pluginMemory = 3;
	public static final int pluginDevice = 4;

	/**
	 * Get the type of the plugin. Plugin can be one of the following
	 * types: <code>pluginCPU</code>, <code>pluginCompiler</code>,
	 * <code>pluginMemory</code> and <code>pluginDevice</code>.
	 * 
	 * @return type of the plugin
	 */
	public int getPluginType ();
	
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
    public String getHash ();
    
}

