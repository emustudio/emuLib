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


public interface IContext {

    /**
     * Return unique ID of this context. This can be any string identifying
     * concrete context. Usually it is related with kind of context
     * (e.g. "cpu8080","mitsSIO-2",...) . Other plugins can identify the
     * context by recognizing of its ID.
     * @return ID of this context.
     */
    public String getID ();
    
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

