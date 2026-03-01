/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins;

import java.util.Objects;
import java.util.Optional;

/**
 * Exception representing general plugin initialization error.
 */
@SuppressWarnings("unused")
public class PluginInitializationException extends Exception {
    /**
     * The plugin that caused the exception.
     */
    private final Plugin plugin;

    /**
     * Constructs a new PluginInitializationException.
     *
     * @param plugin the plugin that caused this exception
     */
    public PluginInitializationException(Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin);
    }

    /**
     * Constructs a new PluginInitializationException.
     *
     * @param plugin  the plugin that caused this exception
     * @param message the detail message
     */
    public PluginInitializationException(Plugin plugin, String message) {
        super(message);
        this.plugin = Objects.requireNonNull(plugin);
    }

    /**
     * Constructs a new PluginInitializationException.
     *
     * @param plugin  the plugin that caused this exception
     * @param message the detail message
     * @param cause   the cause
     */
    public PluginInitializationException(Plugin plugin, String message, Throwable cause) {
        super(message, cause);
        this.plugin = Objects.requireNonNull(plugin);
    }

    /**
     * Constructs a new PluginInitializationException.
     *
     * @param plugin the plugin that caused this exception
     * @param cause  the cause
     */
    public PluginInitializationException(Plugin plugin, Throwable cause) {
        super(cause);
        this.plugin = Objects.requireNonNull(plugin);
    }

    /**
     * Constructs a new PluginInitializationException without plugin reference.
     */
    public PluginInitializationException() {
        plugin = null;
    }

    /**
     * Constructs a new PluginInitializationException without plugin reference.
     *
     * @param message the detail message
     */
    public PluginInitializationException(String message) {
        super(message);
        plugin = null;
    }

    /**
     * Constructs a new PluginInitializationException without plugin reference.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public PluginInitializationException(String message, Throwable cause) {
        super(message, cause);
        plugin = null;
    }

    /**
     * Constructs a new PluginInitializationException without plugin reference.
     *
     * @param cause the cause
     */
    public PluginInitializationException(Throwable cause) {
        super(cause);
        plugin = null;
    }

    /**
     * Gets the plugin that caused this exception, if available.
     *
     * @return the plugin wrapped in an Optional
     */
    public Optional<Plugin> getPlugin() {
        return Optional.ofNullable(plugin);
    }
}
