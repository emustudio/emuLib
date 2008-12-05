package plugins.memory;

import plugins.IPlugin; 
import plugins.ISettingsHandler; 

// <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
// #[regen=yes,id=DCE.FDAF4927-24F7-F865-D6B2-1FD965D52CE9]
// </editor-fold> 
/**
 * This is the main interface that memory plugin should implement.
 */
public interface IMemory extends IPlugin {

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.90113BF3-E553-0EF4-020F-4D8CBD596417]
    // </editor-fold> 
    /**
     * Show GUI of a memory. Every memory plugin should have a GUI, but
     * it is not a duty.
     */
    public void showGUI ();

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.E263CF0E-CAB3-01E4-C6A9-7B40B51D16F8]
    // </editor-fold> 
    /**
     * Gets memory context. Via memory context devices and CPU performs access
     * to memory cells. If memory supports some special techniques (e.g. banking,
     * segmentation, paging, etc.), the context should be extended by new one,
     * that's interface will be public to all interested plugnis or CPUs.
     * @return memory context object
     */
    public IMemoryContext getContext ();

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.518710DE-1BBB-8C56-5401-A79065AB2EE4]
    // </editor-fold> 
    /**
     * Perform initialization process of memory. The memory should physically
     * create the memory - e.g. as an array or something similar. Memory can't
     * use CPU nor devices. It is accesed BY them.
     * @param size      size of the memory, set in architecture configuration
     * @param sHandler  settings handler object. Memory can use this for
     *                  accessing/storing/removing its settings.
     */
    public void initialize (int size, ISettingsHandler sHandler);

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.FBDE8710-8BA4-BA74-CAA9-964DEA669257]
    // </editor-fold> 
    /**
     * Sets program start address. This method is called by main module when
     * compiler finishes compilation process and return known start address of
     * compiled program. This start address is then used by CPU, in reset
     * operation - PC (program counter, or something similar) should be set
     * to this address, accessible via <code>IMemoryContext.getProgramStart()</code>
     * method.
     * @param address  startig memory position (address) of a program
     */
    public void setProgramStart (int address);

}

