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
package emulib.plugins.cpu;

import emulib.annotations.ContextType;
import emulib.plugins.Context;
import emulib.plugins.device.DeviceContext;

/**
 * Basic interface for CPU context. The context is used by plugins, that are
 * connected to CPU.
 *
 * CPU plugins can extend this interface to their own (with some new methods)
 * and then the programmer should make it to be public in order to other plugins
 * could have access to it.
 *
 * Extended context may have methods for e.g. connecting devices to CPU, etc.
 */
@ContextType
public interface CPUContext extends Context {

    /**
     * Determine whether this CPU supports raw interrupts.
     *
     * @return true, if raw interrupts are supported, false otherwise
     */
    boolean isRawInterruptSupported();

    /**
     * Send raw interrupt signal to the CPU.
     *
     * Does nothing if raw interrupts are not supported.
     *
     * @param device Device that interrupts the CPU
     * @param data interrupt data
     */
    void signalRawInterrupt(DeviceContext device, byte []data);

    /**
     * Determine whether this CPU supports interrupts.
     *
     * @return true, if interrupts are supported, false otherwise
     */
    boolean isInterruptSupported();

    /**
     * Send interrupt signal to the CPU.
     *
     * @param device Device that interrupts the CPU
     * @param mask interrupt mask
     */
    void signalInterrupt(DeviceContext device, int mask);

    /**
     * Cancel sending interrupt signal if it was not handled yet.
     *
     * @param device Device that wants to cancel the interrupt signal
     * @param mask clear interrupt mask
     */
    void clearInterrupt(DeviceContext device, int mask);

    /**
     * Get CPU frequency in kHz
     *
     * @return CPU frequency in kHz or 0 if it is not supported
     */
    int getCPUFrequency();

}

