/*
 * KISS, YAGNI, DRY
 *
 * (c) Copyright 2006-2016, Peter Jakubčo
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

package emulib.plugins;

import emulib.annotations.ContextType;
import net.jcip.annotations.ThreadSafe;

/**
 * Context is a functional structure of a plugin that can be used by the other plugins.
 *
 * Plug-ins obtain needed contexts of another plug-ins by querying ContextPool directly. Plugins must be interconnected
 * and compatible to get the contexts.
 *
 * Each plug-in can implement none, one or more contexts, implementing the same interface is allowed. Plug-in can
 * declare new context interface, which would be derived (directly or indirectly) from this interface.
 * 
 * Another requirement is to annotate context interfaces with @ContextType annotation.
 * 
 * Contexts are required to be thread-safe, since there's no guarantee in which threads plug-ins communicate.
 */
@ContextType
@ThreadSafe
public interface Context {

}
