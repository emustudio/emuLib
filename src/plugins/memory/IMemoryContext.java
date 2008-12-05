package plugins.memory;

import java.util.EventListener;
import java.util.EventObject;
import plugins.IContext; 

// <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
// #[regen=yes,id=DCE.5461E378-1625-E6AE-1456-D81A5FA9A6AD]
// </editor-fold> 
/**
 * Interface provides a context for operating memory. It supports basic methods,
 * but if memory wants to support more functionality, this interface should be
 * extended by plugin programmer and he should then make it public, in order to
 * plugins have access to it.
 * 
 * The context is given to plugins (compiler, CPU, devices), that are connected
 * to the memory and they communicate by invoking following methods.
 */
public interface IMemoryContext extends IContext {

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.65A228E6-AF79-B36D-8ACF-D22068E9195F]
    // </editor-fold> 
    /**
     * Read one cell from a memory.
     * @param from  memory position (address) of the read cell
     * @return read cell (don't have to be a byte, therefore its just
     *         <code>Object</code>)
     */
    public Object read (int from);

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.97A571E9-9FC6-D968-2AE1-FC2801EBD27E]
    // </editor-fold> 
    /**
     * Read two cells from a memory at once. Implementation of return value
     * is up to plugin programmer (concatenation of the cells). If cells in
     * memory are pure bytes (java type is e.g. <code>short</code>), concatenation
     * can be realized as (in small endian): 
     * <code>
     *     result = (mem[from]&0xFF) | ((mem[from+1]<<8)&0xFF);
     * </code>
     * 
     * and in big endian as:
     * <code>
     *     result = ((mem[from]<<8)&0xFF) | (mem[from+1]&0xFF);
     * </code>
     * 
     * @param from  memory position (address) of the read cells
     * @return two read cells
     */
    public Object readWord (int from);

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.B9C1ABF5-B183-B691-8EF5-180D6E86AD93]
    // </editor-fold> 
    /**
     * Write one cell-size (e.g. byte) data to a cell to a memory on specified location.
     * @param to   memory position (address) of the cell where data will be written
     * @param val  data to be written
     */
    public void write (int to, Object val);

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.AD5AF081-A61D-F207-8EC5-D755D13B58FB]
    // </editor-fold> 
    /**
     * Write two cell-size (e.g. byte) data to a cell to a memory on specifed
     * location. Implementation of data value is up to plugin programmer
     * (concatenation of the cells) and have to be understandable by memory.
     * @param to   memory position (address) of the read cells
     * @param val  two cells in one <code>Object</code> value
     */
    public void writeWord (int to, Object val);

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.A4436FD0-ABEC-AA01-FBE1-E6D09786A467]
    // </editor-fold> 
    /**
     * Adds the specified memory listener to receive memory events from this memory.
     * Memory events occur even if single cell is changed in memory.
     * If listener is <code>null</code>, no exception is thrown and no action is
     * performed.
     * @param listener  the memory listener
     */
    public void addMemoryListener (IMemoryContext.IMemListener listener);

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.C418BF8C-DB07-8208-08B9-6D94F7B25FD2]
    // </editor-fold> 
    /**
     * Removes the specified memory listener so that it no longer receives memory
     * events from this memory. Memory events occur even if single cell is
     * changed in memory. If listener is <code>null</code>, no exception is 
     * thrown and no action is performed.
     * @param listener  the memory listener to be removed
     */
    public void removeMemoryListener (IMemoryContext.IMemListener listener);

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.67F5ECB5-27D3-112A-834E-1C3A0BAC589E]
    // </editor-fold> 
    /**
     * The listener interface for receiving memory events. The class that is
     * interested in processing a memory event implements this interface, and the
     * object created with that class is registered with a memory, using the
     * memory's <code>addMemoryListener</code> method. Memory events occur even
     * if single cell is changed in memory and then is invoked
     * <code>memChange</code> method.
     */
    public interface IMemListener extends EventListener {

        // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
        // #[regen=yes,id=DCE.7B24926A-B797-6A46-9E57-A43F809899E6]
        // </editor-fold> 
        /**
         * This method is invoked when memory event is occured - when a cell
         * is changed.
         * @param evt  event object
         * @param adr  memory position (address) of changed cell
         */
        public void memChange (EventObject evt, int adr);

    }

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.B2A6B844-00A6-4CB7-2D63-3100EC8DFDBB]
    // </editor-fold> 
    /**
     * Gets size of memory. If memory uses some techniques as banking, real
     * size of the memory is not computed. It is only returned a value set
     * in architecture configuration.
     * @return basic size of the memory
     */
    public int getSize ();

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.02C9185F-7C23-2F64-FF03-DE4A1E6D3C4C]
    // </editor-fold> 
    /**
     * Gets program's start address. The start address is set invoking 
     * memory's method <code>IMemory.setProgramStart()</code> by main module
     * when compiler finishes compilation process of a program and if the compiler
     * know the starting address. This address is used by CPU in resetion process.
     * @return program's start address in memory
     */
    public int getProgramStart ();

}

