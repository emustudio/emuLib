/**
 * ICPUContext.java
 * 
 * (c) Copyright 2008-2010, P. Jakubčo <pjakubco@gmail.com>
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
package plugins.cpu;

import plugins.IContext; 

/**
 * Basic interface for CPU context. The context is used by plugins, that are
 * connected to CPU.
 * 
 * CPU plugins can extend this interface to their own (with some new methods) 
 * and then the programmer should make it to be public in order to other plugins
 * could have access to it.
 * 
 * Extended context may have methods for e.g. connecting devices to CPU, interrupts
 * implementation, etc.
 */
public interface ICPUContext extends IContext {

}

