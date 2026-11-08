/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins;

import net.emustudio.emulib.plugins.annotations.PluginRoot;
import net.emustudio.emulib.runtime.ApplicationApi;

import javax.swing.*;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

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
 * <p>
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
     * Initialize plugin.
     * <p>
     * Called once, after all plugins were created. Here the plugin can request already registered contexts from the
     * {@link ApplicationApi#getContextPool()}.
     *
     * @throws PluginInitializationException thrown when initialization process was not successful
     */
    default void initialize() throws PluginInitializationException {
    }

    /**
     * Reset plugin to its "initial state", as it was after calling {@link Plugin#initialize()}.
     */
    default void reset() {
    }

    /**
     * Destroys all plugin resources (unregister contexts, stop threads, dispose windows).
     * <p>
     * Called when the user closes the emulator.
     */
    default void destroy() {
    }

    /**
     * Show GUI of plugin settings. Called only if {@link #isShowSettingsSupported()} returns true.
     *
     * @param parent emuStudio main window
     */
    default void showSettings(JFrame parent) {
    }

    /**
     * Check if {@link #showSettings(JFrame)} is supported by this plugin.
     *
     * @return true if the plugin has a GUI for settings; false otherwise
     */
    default boolean isShowSettingsSupported() {
        return false;
    }

    /**
     * Get run-time title of this plugin. Defaults to {@link PluginRoot#title()}.
     *
     * @return run-time title of the plugin
     */
    default String getTitle() {
        return getClass().getAnnotation(PluginRoot.class).title();
    }

    /**
     * Get plugin short description. Defaults to {@link PluginRoot#description()}.
     *
     * @return plugin description
     */
    default String getDescription() {
        return getClass().getAnnotation(PluginRoot.class).description();
    }

    /**
     * Get version of the plugin, read as {@code version} key from a {@code version.properties} resource bundle in the
     * plugin class's package. A missing bundle or key returns {@code "(unknown)"}.
     *
     * @return version string
     */
    default String getVersion() {
        return readMetadata("version");
    }

    /**
     * Get copyright string, read as {@code copyright} key from the same resource bundle as {@link #getVersion()}.
     *
     * @return copyright string
     */
    default String getCopyright() {
        return readMetadata("copyright");
    }

    private String readMetadata(String key) {
        Class<?> pluginClass = getClass();
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(
                    pluginClass.getPackageName() + ".version", Locale.getDefault(), pluginClass.getClassLoader());
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return "(unknown)";
        }
    }

    /**
     * Determines if this plugin supports emulation automation.
     *
     * @return true if the plugin supports emulation automation; false otherwise
     */
    default boolean isAutomationSupported() {
        return false;
    }
}
