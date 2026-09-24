/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Reads plugin metadata (version, copyright) from a {@code version} resource bundle.
 * <p>
 * The bundle base name is derived from the plugin class package
 * ({@code <package>.version}) and is loaded using the plugin class's own classloader,
 * so it resolves correctly under the isolated plugin classloaders (see ADR-0006).
 */
public final class PluginMeta {
    /**
     * Value returned when the metadata cannot be resolved.
     */
    public static final String UNKNOWN = "(unknown)";

    private PluginMeta() {
    }

    /**
     * Returns the plugin version, or {@link #UNKNOWN} if it cannot be resolved.
     *
     * @param pluginClass the concrete plugin class
     * @return version string
     */
    public static String version(Class<?> pluginClass) {
        return read(pluginClass, "version");
    }

    /**
     * Returns the plugin copyright, or {@link #UNKNOWN} if it cannot be resolved.
     *
     * @param pluginClass the concrete plugin class
     * @return copyright string
     */
    public static String copyright(Class<?> pluginClass) {
        return read(pluginClass, "copyright");
    }

    private static String read(Class<?> pluginClass, String key) {
        try {
            ResourceBundle bundle = ResourceBundle.getBundle(
                    pluginClass.getPackageName() + ".version", Locale.getDefault(), pluginClass.getClassLoader());
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return UNKNOWN;
        }
    }
}
