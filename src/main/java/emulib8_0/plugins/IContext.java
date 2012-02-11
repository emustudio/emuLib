/**
 *
 * IContext.java
 * 
 * (c) Copyright 2008-2010 P. Jakubčo <pjakubco@gmail.com>
 * 
 * KISS, YAGNI
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

package emulib8_0.plugins;

/**
 * Context is a sub-structure of a plugin that represents functionality that
 * can be used by the other plugins. Plug-ins can ask emuLib for contexts of
 * another plug-ins directly. If they are allowed to get the context, emuLib
 * returns the object (if it is found).
 *
 * Each plug-in can have none, one or more than one context, even implementing
 * the same interface. Plug-ins can extend standard context. Then they must
 * give speific name to it - "emustudio.context.CONTEXT_NAME", where
 * CONTEXT_NAME must start with a letter "C" following 40-byte long SHA-1 hash
 * of all its method names.
 *
 * Hash string is computed from names of all  methods. Hash is computed from
 * a string that consists of alphabetically sorted names of all context methods
 * with their return type and parameter types (returned by calling the method
 * Method.toGenericString()).
 * 
 * A single method is written in the following way:
 * "type name(parameter_type1,parameter_type2,...)"
 *
 * Methods are then sorted alphabetically in ascending, and joined together
 * with separator ";", like this:
 * "method1;method2;..."
 *
 * For example: "String getID();void setRAM(int,int)"
 *
 * Then the string has to be hashed by SHA-1 hash.
 * The hash is used for identifying the context by emuLib and the other plugins.
 *
 * @author Peter Jakubčo <pjakubco at gmail dot com>
 */
public interface IContext {

    /**
     * Get specific ID of the context. If the plug-in has more than one
     * implementation of the same context interface, the implementations
     * should vary by assingning different ID strings to them.
     *
     * The IDs should be clear and short (one-two words separated by a
     * dash "-"), e.g. "data-port", or "control-port".
     *
     * If there is a single implementation of the context interface, the
     * ID can be also null. But it is not recomended.
     *
     * @return ID of the context
     */
    public String getID();

}
