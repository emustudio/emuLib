/*
 * Run-time library for emuStudio and plugins.
 *
 *     Copyright (C) 2006-2020  Peter Jakubčo
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.emustudio.emulib.plugins.device;

import net.emustudio.emulib.plugins.annotations.PluginContext;
import net.emustudio.emulib.plugins.Context;
import java.io.IOException;

/**
 * Interface for basic context of the device. If device support more
 * functionality than input or output, it should be extended (or implemented
 * by an abstract class), and then made public.
 * 
 * @param <T> Type of data which can be transferred to/from this device
 */
@SuppressWarnings("unused")
@PluginContext
public interface DeviceContext<T> extends Context {

    /**
     * Read data from the device.
     *
     * From the CPU point of view, this should be an implementation of
     * the "IN" operation. The device should return one elementary unit
     * (e.g. byte) of its input data. I/O operations
     * are considered as events that occurred onto this device.
     *
     * @return elementary data read from device
     * @throws IOException if something goes wrong
     */
    T read () throws IOException;

    /**
     * Writes/sends data to the device.
     *
     * From the CPU point of view, this should be an implementation of the
     * "OUT" operation. The device should accept one elementary unit
     * (e.g. byte) of the output data. I/O operations are
     * considered as events that occurred onto this device.
     *
     * @param val  data to be written to the device
     * @throws IOException if something goes wrong
     */
    void write (T val) throws IOException;

    /**
     * Get the type of transferred data. As you can see, methods
     * <code>read</code> and <code>write</code> use <code>Object</code> as
     * the transferred data type. This method specifies the data type.
     *
     * @return type of transferred data
     */
    Class<T> getDataType();

}

