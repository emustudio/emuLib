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

package plugins;

/**
 * Context is a sub-structure of a plugin that represents functionality that
 * can be used by other plugins. If a plugin is connected to another plugin,
 * female-plug plugin gets the context of the connecting plugin.
 * 
 * If the plugin is a device, then the context represent one connection port,
 * therefore devices can have more than one context.
 * 
 * Only compiler plugins do not have a context. Each other plugin has to have
 * at least one context.
 *
 * @author Peter Jakubčo <pjakubco at gmail.com>
 */
public interface IContext {
    
    /**
     * Hash string (doesn't matter how long) is computed from names of all
     * methods. Hash is computed from a string that consists of alphabetically
     * sorted names of all context methods with their return type and parameter
     * types.
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
     * Then the string has to be hashed by md5 hash.
     * 
     * The hash is used for identifying the context by other plugins.
     * 
     * @return hash of all context methods
     */
    public String getHash();

}

