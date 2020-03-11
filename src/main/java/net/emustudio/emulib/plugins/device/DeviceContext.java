// SPDX-License-Identifier: GPL-3.0-or-later
package net.emustudio.emulib.plugins.device;

import net.emustudio.emulib.plugins.annotations.PluginContext;
import net.emustudio.emulib.plugins.Context;

import java.io.IOException;

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
     * @throws IOException if something goes wrong
     */
    DataType readData() throws IOException;

    /**
     * Writes/sends data to the device.
     * <p>
     * From the CPU point of view, this is what the "OUT" instruction should use. The device should accept one
     * elementary unit (e.g. a byte) of the output data. I/O operations are considered as events that occurred
     * within this device.
     *
     * @param value data to be written to the device
     * @throws IOException if something goes wrong
     */
    void writeData(DataType value) throws IOException;

    /**
     * Get the type of transferred data.
     *
     * @return type of transferred data
     */
    Class<DataType> getDataType();
}

