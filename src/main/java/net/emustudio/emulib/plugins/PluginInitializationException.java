// SPDX-License-Identifier: GPL-3.0-or-later
package net.emustudio.emulib.plugins;

import java.util.Objects;
import java.util.Optional;

/**
 * Exception representing general plugin initialization error.
 */
@SuppressWarnings("unused")
public class PluginInitializationException extends Exception {
    private final Plugin plugin;

    public PluginInitializationException(Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin);
    }

    public PluginInitializationException(Plugin plugin, String message) {
        super(message);
        this.plugin = Objects.requireNonNull(plugin);
    }

    public PluginInitializationException(Plugin plugin, String message, Throwable cause) {
        super(message, cause);
        this.plugin = Objects.requireNonNull(plugin);
    }

    public PluginInitializationException(Plugin plugin, Throwable cause) {
        super(cause);
        this.plugin = Objects.requireNonNull(plugin);
    }

    public PluginInitializationException() {
        plugin = null;
    }

    public PluginInitializationException(String message) {
        super(message);
        plugin = null;
    }

    public PluginInitializationException(String message, Throwable cause) {
        super(message, cause);
        plugin = null;
    }

    public PluginInitializationException(Throwable cause) {
        super(cause);
        plugin = null;
    }

    public Optional<Plugin> getPlugin() {
        return Optional.ofNullable(plugin);
    }
}
