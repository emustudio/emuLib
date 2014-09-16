/*
 * KISS, YAGNI, DRY
 *
 * (c) Copyright 2008-2014, Peter Jakubčo
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

/**
 * Context is a functional structure of a plugin that can be used by the other plugins.
 *
 * Plug-ins ask emuLib to get contexts of another plug-ins, by querying ContextController directly. If they are allowed
 * to get the context, emuLib returns the object (if it is found).
 *
 * Each plug-in can have none, one or more contexts, even implementing the same interface. Context interface can extend
 * standard context interfaces, provided by emuLib (such as CPUContext, MemoryContext, etc.). The last requirement is
 * to annotate context interfaces with @ContextType annotation.
 */
@ContextType
public interface Context {

}
