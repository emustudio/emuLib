/*
 * Run-time library for emuStudio and plug-ins.
 *
 *     Copyright (C) 2006-2020  Peter Jakubčo
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.emustudio.emulib.plugins;

import net.emustudio.emulib.plugins.annotations.PluginContext;
import net.jcip.annotations.ThreadSafe;

/**
 * A plugin context is a runtime structure implemented and used by plugins in order to communicate between each other.
 *
 * At first, plugins should register their contexts in the plugin root class constructor. Then, within plugin
 * initialization, it can requests contexts of other plugins.
 *
 * Whether they get the requested context is given by several factors, like the plugins must be interconnected in
 * the abstract schema, and the contexts must be compatible.
 *
 * Each plugin can implement none, one or more contexts. Implementing the same interface multiple times is allowed.
 * A plugin can also extend this context interface (directly or indirectly).
 * 
 * Another requirement is to annotate custom context interfaces with {@link PluginContext} annotation.
 * 
 * Contexts should be thread-safe, since there's no guarantee in which threads plugins communicate.
 */
@PluginContext
@ThreadSafe
public interface Context {

}
