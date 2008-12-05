package plugins.cpu;

import java.util.EventListener;
import java.util.EventObject;
import plugins.IContext; 

// <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
// #[regen=yes,id=DCE.F6CE8CDD-D44F-97F1-EA9D-613D6755D39A]
// </editor-fold> 
/**
 * Basic interface for CPU context. The context is used by plugins, that are
 * connected to CPU.
 * 
 * CPU plugins can extend this interface to their own (with some new methods) 
 * and then the programmer should make it to be public in order to other plugins
 * could have access to it.
 * 
 * Extended context may have methods for e.g. connecting devices to CPU, interrupts
 * implementation, setting frequency, etc.
 */
public interface ICPUContext extends IContext {

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.4746319D-A6FE-C7B5-E873-DBB02A81EDD3]
    // </editor-fold> 
    /**
     * Get actual instruction position (before its execution). Can be said,
     * that this method should return PC (program counter) register (if CPU
     * has one).
     * @return memory position (address) of next instruction
     */
    public int getInstrPosition ();

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.6FB549D6-9B92-2D5D-F1A5-0C5CF4A2BB25]
    // </editor-fold> 
    /**
     * Method compute address of an instruction that follows after instruction
     * defined by given address. Main module uses this method to determine
     * on which address should start next instruction in debug window. Several
     * calls of this method make possible to create a list of instructions that
     * begin on arbitrary address (debug window table).
     * @param pos  memory position (address) of an instruction
     * @return address of an instruction followed by specified address
     */
    public int getNextInstrPos (int pos);

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.B87678C6-5003-B76B-A684-BAE73111301F]
    // </editor-fold> 
    /**
     * Set new actual instruction position (that will be executed as next). It
     * can be said, that a parameter represents new value of PC (program counter),
     * if CPU has one. Otherwise CPU should interpret the position in the right
     * manner. 
     * 
     * This method is called by main module when user perform "jump to address"
     * operation.
     * @param pos  new address of actual instruction
     * @return true if operation was successful, false otherwise
     */
    public boolean setInstrPosition (int pos);

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.0D545D63-9979-B9C0-D1D2-8429CE215429]
    // </editor-fold> 
    /**
     * Adds the specified CPU listener to receive CPU events from this CPU.
     * CPU events occur when CPU changes its state, or run state. CPU state
     * events don't occur if CPU is running, only happens with run state changes.
     * If listener is <code>null</code>, no exception is thrown and no action is
     * performed.
     * @param listener  the CPU listener
     */
    public void addCPUListener (ICPUContext.ICPUListener listener);

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.3CC201D3-AEA9-4AA0-0354-B0DFD894331B]
    // </editor-fold> 
    /**
     * Removes the specified CPU listener so that it no longer receives CPU
     * events from this CPU. CPU events occur when CPU changes its state, or run
     * state. CPU state events don't occur if CPU is running, only happens with
     * run state changes. If listener is <code>null</code>, no exception is 
     * thrown and no action is performed.
     * @param listener  the CPU listener to be removed
     */
    public void removeCPUListener (ICPUContext.ICPUListener listener);

    // <editor-fold defaultstate="collapsed" desc=" UML Marker ">
    // #[regen=yes,id=DCE.22934380-D6C4-4764-B51C-5D8C17D9C46C]
    // </editor-fold>
    /**
     * Determine whether breakpoints are supported by CPU.
     * @return true if breakpoints are supported, false otherwise
     */
    public boolean isBreakpointSupported ();

    // <editor-fold defaultstate="collapsed" desc=" UML Marker ">
    // #[regen=yes,id=DCE.91AC5518-6503-4A36-0B6F-E76DBDC1AB60]
    // </editor-fold>
    /**
     * Set/unset a breakpoint to specified memory position (address). It should
     * be called only if breakpoints are supported
     * (<code>isBreakpointSupported()</code>).
     * @param pos  memory address where breakpoint should be set/unset
     * @param set  true if breakpoint should be set, false otherwise
     */
    public void setBreakpoint (int pos, boolean set);

    // <editor-fold defaultstate="collapsed" desc=" UML Marker ">
    // #[regen=yes,id=DCE.BC871301-D62D-BF1B-9BCC-F460E76A6D59]
    // </editor-fold>
    /**
     * Determine breakpoint on specified address. It should be called only if
     * breakpoints are supported (<code>isBreakpointSupported()</code>), otherwise
     * the method should return false always.
     * @param pos  memory position (address), from where we try to determine
     *             breakpoint
     * @return true if breakpoint exists on specified address, false otherwise
     */
    public boolean getBreakpoint (int pos);


    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.1A19EB3D-5F6D-ADAC-7893-1367282844E8]
    // </editor-fold> 
    /**
     * The listener interface for receiving CPU events. The class that is
     * interested in processing a CPU event implements this interface, and the
     * object created with that class is registered with a CPU, using the CPU's
     * addCPUListener method.
     * When the CPU event occurs, that:
     * <ul>
     *     <li>if the event is CPU's state change, then object's <code>stateUpdated()</code>
     *         method is invoked.</li>
     *     <li>if the event is CPU's run state change, then object's <code>runChanged()</code>
     *         method is invoked.</li>
     * </ul>
     */
    public interface ICPUListener extends EventListener {

        // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
        // #[regen=yes,id=DCE.16877133-B660-4267-16CA-4FD275792E2C]
        // </editor-fold> 
        /**
         * Invoked when an CPU's run state changes.
         * @param evt    event object
         * @param state  new run state of the CPU
         */
        public void runChanged (EventObject evt, ICPUContext.stateEnum state);

        // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
        // #[regen=yes,id=DCE.3F91E9A6-BDF9-86C4-8E91-812761DA09C6]
        // </editor-fold> 
        /**
         * Invoked when an CPU's state changes. The state can be register change,
         * flags change, or other CPU's internal state change.
         * @param evt  event object
         */
        public void stateUpdated (EventObject evt);

    }

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.DA5AB08E-50B5-3ADD-DE06-4C12D6F7CA22]
    // </editor-fold> 
    /**
     * Enumeration for available run states of CPU.
     */
    public enum stateEnum {

        // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
        // #[regen=yes,id=DCE.4AABD8AD-CFA8-6722-165F-B80731295173]
        // </editor-fold> 
        /**
         * CPU is stopped (naturally or by user) and should not be run until its
         * reset.
         */
        stoppedNormal,

        // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
        // #[regen=yes,id=DCE.6C4142B4-500A-C0C3-47BA-7D8F61132928]
        // </editor-fold> 
        /**
         * CPU is in breakpoint state (paused).
         */
        stoppedBreak,

        // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
        // #[regen=yes,id=DCE.7A085F1E-E86D-69D5-EC50-5C9EB21AB84F]
        // </editor-fold> 
        /**
         * CPU is stopped because of address fallout error. It should not be
         * run until its reset.
         */
        stoppedAddrFallout,

        // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
        // #[regen=yes,id=DCE.908689D4-1BF0-D77C-5428-E1E0750CFB33]
        // </editor-fold> 
        /**
         * CPU is stopped because of instruction fallout (unknown istruction) 
         * error. It should not be run until its reset.
         */
        stoppedBadInstr,

        // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
        // #[regen=yes,id=DCE.4198583C-DBB6-3A5D-BB93-DEC5C5A9F227]
        // </editor-fold> 
        /**
         * CPU is running.
         */
        runned;


    }

}

