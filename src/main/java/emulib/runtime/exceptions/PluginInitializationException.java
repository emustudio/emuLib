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
package emulib.runtime.exceptions;

import emulib.plugins.Plugin;
import java.util.Objects;
import java.util.Optional;

/**
 * Exception representing general plug-in initialization error.
 */
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
