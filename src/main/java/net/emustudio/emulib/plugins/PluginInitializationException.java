/*
 * This file is part of emuLib.
 *
 * Copyright (C) 2006-2023  Peter Jakubčo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
