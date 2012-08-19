/*
 * InvalidPluginException.java
 *
 * KISS, YAGNI, DRY
 * 
 * (c) Copyright 2012, Peter Jakubčo
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
package emulib.runtime;

/**
 * This class represents an exception that can be raised during PluginLoader.loadPlugin method if a main plug-in class
 * does not meet requirements for Plugin classes.
 * 
 */
public class InvalidPluginException extends RuntimeException {
    
    public InvalidPluginException() {
        
    }
    
    public InvalidPluginException(String cause) {
        super(cause);
    }
    
    public InvalidPluginException(String cause, Throwable e) {
        super(cause, e);
    }
    
}
