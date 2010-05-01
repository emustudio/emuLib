/**
 * IDevice.java
 * 
 * (c) Copyright 2008-2009, P. Jakubčo <pjakubco@gmail.com>
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
package plugins.device;

import plugins.IPlugin; 
import plugins.memory.IMemoryContext; 
import plugins.ISettingsHandler; 
import plugins.cpu.ICPUContext; 

/**
 * Main interface that has to be implemented by device plugin.

 * Design of the interface supports hierarchical connection of devices. Devices
 * identifies each other by context methods <code>getID()</code>,
 * and <code>getHash()</code>. The connection request can be accepted or 
 * rejected if attaching device is/isn't supported. Devices that don't support
 * any connection hierarchy, invoking their <code>attachDevice()</code> method,
 * always return <code>true</code>.
 */
public interface IDevice extends IPlugin {

    /**
     * Perform initialization process of this device. It is called by main module
     * after successful initialization of compiler, CPU and memory. Device should
     * initialize itself besides other things also in a way of checking supported
     * CPU and memory.
     * @param cpu       context of a CPU. Device should check this for extended
     *                  context. Will be <code>null</code> if a device is not
     *                  connected to CPU.
     * @param mem       context of a memory. Device should check this for 
     *                  extended context. Will be <code>null</code> if a device
     *                  is not connected to memory.
     * @param sHandler  settings handler object. Device can use this for
     *                  accessing/storing/removing its settings.
     * @return true if initialization process was successful
     */
    public boolean initialize (ICPUContext cpu, IMemoryContext mem, ISettingsHandler sHandler);

    /**
     * Shows GUI of a device. Device don't have to have a GUI, but instead it
     * should display information message. 
     */
    public void showGUI ();

    /**
     * Perform a connection of two devices. Male is a context of 
     * another device (that want to be connected into this one).
     * Method <strong>should carefully</strong> check if male can
     * be connected to this device, by recognizing its context. This
     * is the only way how to ensure correctness of the connection.
     * 
     * @param male    male-plug device context
     * @return true if connection process was successful, false otherwise
     */
    public boolean attachDevice (IDeviceContext male);

    /**
     * Detach all devices from this device. This method is invoked by main module
     * in application closing process.
     */
    public void detachAll ();
    
    /**
     * Get next context of this device. The device can have more than one
     * context. In connection process, the main module asks for next
     * device context for each connection case. If the device
     * has only one context, it should return only this context for each
     * call of this method.
     * @return next device context
     */
    public IDeviceContext getNextContext();

}

