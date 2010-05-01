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

import java.util.EventListener;
import java.util.EventObject;
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
	
    /**
     * Adds the specified CPU listener to receive CPU events from this CPU.
     * CPU events occur when CPU changes its state, or run state. CPU state
     * events don't occur if CPU is running, only happens with run state changes.
     * If listener is <code>null</code>, no exception is thrown and no action is
     * performed.
     * @param listener  the CPU listener
     */
    public void addCPUListener (ICPUContext.ICPUListener listener);

    /**
     * Removes the specified CPU listener so that it no longer receives CPU
     * events from this CPU. CPU events occur when CPU changes its state, or run
     * state. CPU state events don't occur if CPU is running, only happens with
     * run state changes. If listener is <code>null</code>, no exception is 
     * thrown and no action is performed.
     * @param listener  the CPU listener to be removed
     */
    public void removeCPUListener (ICPUContext.ICPUListener listener);

    /**
     * The listener interface for receiving CPU events. The class that is
     * interested in processing a CPU event implements this interface, and the
     * object created with that class is registered with a CPU, using the CPU's
     * addCPUListener method.
     * When the CPU event occurs, that:
     * <ul>
     *     <li>if the event is CPU's state change, then object's <code>stateUpdated()</code>
     *         method is invoked.</li>
     *     <li>if the event is CPU's run state change, then object's <code>runChanged()</code>
     *         method is invoked.</li>
     * </ul>
     */
    public interface ICPUListener extends EventListener {

        /**
         * Invoked when an CPU's run state changes.
         * @param evt       event object
         * @param runState  new run state of the CPU
         */
        public void runChanged (EventObject evt, int runState);

        /**
         * Invoked when an CPU's state changes. The state can be register change,
         * flags change, or other CPU's internal state change.
         * @param evt  event object
         */
        public void stateUpdated (EventObject evt);

    }

}

