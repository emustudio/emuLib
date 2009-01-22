/**
 * IMemory.java
 * 
 * (c) Copyright 2008-2009, P.Jakubčo
 * 
 * KISS,YAGNI
 * 
 */
package plugins.memory;

import plugins.IPlugin; 
import plugins.ISettingsHandler; 

/**
 * This is the main interface that memory plugin should implement.
 */
public interface IMemory extends IPlugin {

    /**
     * Show GUI of a memory. Every memory plugin should have a GUI, but
     * it is not a duty.
     */
    public void showGUI ();

    /**
     * Gets memory context. Via memory context devices and CPU performs access
     * to memory cells. If memory supports some special techniques (e.g. banking,
     * segmentation, paging, etc.), the context should be extended by new one,
     * that's interface will be public to all interested plugnis or CPUs.
     * @return memory context object
     */
    public IMemoryContext getContext ();

    /**
     * Perform initialization process of memory. The memory should physically
     * create the memory - e.g. as an array or something similar. Memory can't
     * use CPU nor devices. It is accessed BY them.
     * @param size      size of the memory, set in architecture configuration
     * @param sHandler  settings handler object. Memory can use this for
     *                  accessing/storing/removing its settings.
     *
     * @return true if initialization process was successful, false otherwise
     */
    public boolean initialize (int size, ISettingsHandler sHandler);

    /**
     * Sets program start address. This method is called by main module when
     * compiler finishes compilation process and return known start address of
     * compiled program. This start address is then used by CPU, in reset
     * operation - PC (program counter, or something similar) should be set
     * to this address, accessible via <code>IMemoryContext.getProgramStart()</code>
     * method.
     * @param address  starting memory position (address) of a program
     */
    public void setProgramStart (int address);

    /**
     * Gets size of memory. If memory uses some techniques as banking, real
     * size of the memory is not computed. It is only returned a value set
     * in architecture configuration.
     * @return basic size of the memory
     */
    public int getSize ();

    /**
     * Gets program's start address. The start address is set invoking 
     * memory's method <code>IMemory.setProgramStart()</code> by main module
     * when compiler finishes compilation process of a program and if the compiler
     * know the starting address. This address is used by main module for
     * CPU reset process.
     * 
     * @return program's start address in memory
     */
    public int getProgramStart ();

}

