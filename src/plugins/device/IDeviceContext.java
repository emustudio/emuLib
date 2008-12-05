package plugins.device;

import java.util.EventListener;
import java.util.EventObject;
import plugins.IContext; 

/**
 * Interface for basic context of the device. If device support more functionality
 * than input or output, it should be extended an then make public.
 */
// <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
// #[regen=yes,id=DCE.CE696B41-3B8C-9E41-4158-EEE3BE149D5F]
// </editor-fold> 
public interface IDeviceContext extends IContext, EventListener {

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.EE0F6F96-BBDE-FFFB-F641-AC15C3578DC3]
    // </editor-fold> 
    /**
     * Perform "IN" operation, it reads data from this device. The device should
     * return one byte of its input data. I/O operations are considered as
     * events that occured to this device.
     * @param evt  event object
     * @return input data read from device
     */
    public int in (EventObject evt);

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.1EFBD6E7-034E-9B5D-21D8-2314EED897E6]
    // </editor-fold> 
    /**
     * Perform "OUT" operation, it writes a data to this device. The device schould
     * accept one byte of the data that parameter val offers. I/O operations are
     * considered as events that occured to this device.
     * @param evt  event object
     * @param val  data to be written to a device
     */
    public void out (EventObject evt, int val);

}

