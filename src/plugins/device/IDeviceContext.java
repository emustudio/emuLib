/**
 * IDeviceContext.java
 * 
 * (c) Copyright 2008-2010, P.Jakubčo <pjakubco@gmail.com>
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

import java.util.EventListener;
import java.util.EventObject;
import plugins.IContext; 

/**
 * Interface for basic context of the device. If device support more functionality
 * than input or output, it should be extended (or implemented by an abstract class),
 * and then make public.
 */
public interface IDeviceContext extends IContext, EventListener {

    /**
     * Perform "IN" operation, it reads data from this device. The device should
     * return one byte of its input data. I/O operations are considered as
     * events that occurred to this device.
     * @param evt  event object
     * @return input data read from device
     */
    public Object in (EventObject evt);

    /**
     * Perform "OUT" operation, it writes a data to this device. The device should
     * accept one byte of the data that parameter <code>val</code> offers. I/O operations are
     * considered as events that occurred to this device.
     * @param evt  event object
     * @param val  data to be written to a device
     */
    public void out (EventObject evt, Object val);
    
    /**
     * Get the type of transferred data. As you can see, methods <code>in</code> and
     * <code>out</code> use <code>Object</code> as the data type. This method should
     * make the data type specific.
     * @return data type of transferred data
     */
    public Class getDataType();

}

