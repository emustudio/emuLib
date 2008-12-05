package plugins.device;

import plugins.IPlugin; 
import plugins.memory.IMemoryContext; 
import plugins.ISettingsHandler; 
import plugins.cpu.ICPUContext; 

// <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
// #[regen=yes,id=DCE.F9B8D4E2-92A1-41A8-567F-9F4E2CEED912]
// </editor-fold> 
/**
 * Main interface that has to be implemented by device plugin.

 * Design of the interface supports hierarchical connection of devices. Devices
 * identifies each other by context methods <code>getID()</code>,
 * <code>getVersionMajor()</code>, <code>getVersionMinor()</code> and 
 * <code>getVersionRev()</code>. The connection request can be accepted or 
 * rejected if attaching device is/isn't supported. Devices that don't support
 * any connection hierarchy, invoking their <code>attachDevice()</code> method,
 * always return <code>false</code>.
 */
public interface IDevice extends IPlugin {

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.F0634426-3CD2-C5D9-62D1-2AECDF82358E]
    // </editor-fold> 
    /**
     * Perform initialization process of this device. It is called by main module
     * after successful initialization of compiler, cpu and memory. Device should
     * initialize itself besides other things also in a way of checking supported
     * cpu and memory.
     * @param cpu       context of a CPU. Device should chceck this for extended
     *                  context. Will be <code>null</code> if a device is not
     *                  connected to CPU.
     * @param mem       context of a memory. Device should chceck this for 
     *                  extended context. Will be <code>null</code> if a device
     *                  is not connected to memory.
     * @param sHandler  settings handler object. Device can use this for
     *                  accessing/storing/removing its settings.
     * @return true if initialization process was successful
     */
    public boolean initialize (ICPUContext cpu, IMemoryContext mem, ISettingsHandler sHandler);

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.2ED4D172-CBBE-6AE2-3A3F-96F7CD0686B6]
    // </editor-fold> 
    /**
     * Shows GUI of a device. Device don't have to have a GUI, but instead it
     * should display information message. 
     */
    public void showGUI ();

    /**
     * Gets free female-plug device context to that can be attached another device(s).
     * If it is <code>null</code>, no device can be plugged into this device.
     * It is called by main module in connection process if devices (this
     * and another) are connected in abstract scheme.
     * @return free female-plug device context
     */
    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.5A71895A-1007-C163-61AA-7BB2E6C75056]
    // </editor-fold> 
    public IDeviceContext getFreeFemale ();

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.FA74C294-72D9-DCEE-0DC5-4B94300AE11A]
    // </editor-fold> 
    /**
     * Gets free male-plug device context that can be attached to another device.
     * If it is <code>null</code>, the device can not be plugged into no device.
     * It is called by main module in connection process if devices (this
     * and another) are connected in abstract scheme.
     * @return free male-plug device context
     */
    public IDeviceContext getFreeMale ();

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.F466A2F8-3535-91B2-416C-31FD21A693B1]
    // </editor-fold> 
    /**
     * Perform a connection of two devices. Female should be female-plug
     * device context of this device (determined by method
     * <code>getFreeFemale()</code>), male is context of another device.
     * Method <strong>should carefully</strong> check if both female and
     * male can be connected together by recognizing their contexts. This
     * is the only way how to ensure correctness of the connection.
     * 
     * @param female  female-plug device context
     * @param male    male-plug device context
     * @return true if connection process was successful, false otherwise
     */
    public boolean attachDevice (IDeviceContext female, IDeviceContext male);

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.929FCCFE-7EB1-FFAF-80E7-C02810E40421]
    // </editor-fold> 
    /**
     * Detach a device from this device. This method is invoked by main module
     * in ending process.
     * 
     * @param device  device that wants to be detached from this device
     * @param male    true, if detached device is a male-plug, false if it is
     *                a female-plug
     */
    public void detachDevice (IDeviceContext device, boolean male);

}

