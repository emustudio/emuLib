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
 * Context is a functional structure of a plugin that can be used by the other plugins.
 *
 * plugins obtain needed contexts of another plugins by querying ContextPool directly. plugins must be interconnected
 * and compatible to get the contexts.
 *
 * Each plugin can implement none, one or more contexts, implementing the same interface is allowed. plugin can
 * declare new context interface, which would be derived (directly or indirectly) from this interface.
 * 
 * Another requirement is to annotate context interfaces with @ContextType annotation.
 * 
 * Contexts are required to be thread-safe, since there's no guarantee in which threads plugins communicate.
 */
@PluginContext
@ThreadSafe
public interface Context {

}
