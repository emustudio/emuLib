/*
 * This file is part of emuLib.
 *
 * Copyright (C) 2006-2020  Peter Jakubčo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.emustudio.emulib.plugins.device;

import net.emustudio.emulib.plugins.Context;
import net.emustudio.emulib.plugins.annotations.PluginContext;

/**
 * Device context of the device plugin. If the device supports more functionality, it can be extended. Device plugins
 * can have multiple device contexts. Plugins which need the specific device contexts should declare a dependency
 * on the device plugin.
 *
 * @param <DataType> Type of data which can be transferred from/to this device
 */
@SuppressWarnings("unused")
@PluginContext
public interface DeviceContext<DataType> extends Context {

    /**
     * Read data from the device.
     * <p>
     * From the CPU point of view, this should be an implementation of
     * the "IN" operation. The device should return one elementary unit
     * (e.g. byte) of its input data. I/O operations
     * are considered as events that occurred onto this device.
     *
     * @return elementary data read from device
     * @throws RuntimeException if something goes wrong
     */
    DataType readData();

    /**
     * Writes/sends data to the device.
     * <p>
     * From the CPU point of view, this is what the "OUT" instruction should use. The device should accept one
     * elementary unit (e.g. a byte) of the output data. I/O operations are considered as events that occurred
     * within this device.
     *
     * @param value data to be written to the device
     * @throws RuntimeException if something goes wrong
     */
    void writeData(DataType value);

    /**
     * Get the type of transferred data.
     *
     * @return type of transferred data
     */
    Class<DataType> getDataType();
}

