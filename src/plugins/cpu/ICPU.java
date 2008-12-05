package plugins.cpu;

import javax.swing.JPanel; 
import plugins.memory.IMemoryContext;
import plugins.IPlugin; 
import plugins.ISettingsHandler; 

// <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
// #[regen=yes,id=DCE.8077D3BD-16EA-AB4A-05C1-4AFA137A2043]
// </editor-fold> 
/**
 * Interface that covers CPU. This is the main interface that CPU plugin
 * has to implement.
 */
public interface ICPU extends IPlugin {

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.7D11EC9A-AFA0-2DA9-DF03-1E847F1366A2]
    // </editor-fold> 
    /**
     * Perform initialization of CPU. This method is called after compiler
     * successful initialization. Initialization process of CPU can be
     * various, e.g. check for memory type, retrieve some settings from
     * config file, etc.
     * @param mem       memory context that this CPU should use. If CPU and memory
     *                  aren't connected in abstract scheme, this will be
     *                  <code>null</code>. Plugin should therefore check this
     *                  variable and in the case of null memory and if CPU can't
     *                  work without memory, plugin should display error message
     *                  and then return false.
     * @param sHandler  settings handler object. CPU can use this for
     *                  accessing/storing/removing its settings.
     * @return true if initialization was successful, false otherwise
     */
    public boolean initialize (IMemoryContext mem, ISettingsHandler sHandler);

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.16A7164F-9DDE-F2E0-9842-8F0E24BE8382]
    // </editor-fold> 
    /**
     * Perform one step of CPU emulation. It means that one instruction should
     * be executed. CPU state changes to state "running", then it executes one
     * instruction, and then it should return to state "breakpoint" or "stopped".
     * Correct timing of runned instruction isn't so important.
     */
    public void step ();

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.04E4BF5F-EA8A-6549-D727-907EF2E9CEE7]
    // </editor-fold> 
    /**
     * Runs CPU emulation. Change state of CPU to "running" and start
     * instruction fetch/decode/execute loop. Best for this purpose is to create
     * a separate thread that runs permanently and protect emulator from "freeze",
     * the cause of started instruction execution loop. It should be possible
     * to control the thread in future by other methods: <code>pause()</code>
     * and <code>stop()</code>. Therefore after CPU's run the execution should
     * return from this method. It can be assumed that while CPU is in run state,
     * main module won't allow to call method <code>step()</code>.
     * 
     * In CPU run state (a good CPU) performs right timing for instructions.
     * Debug window isn't updated after execution of each instruction, so
     * execution loop should be faster as it is by calling <code>step</code>
     * method.
     */
    public void execute ();

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.DA479300-3825-3056-F6E4-4DD118C06199]
    // </editor-fold> 
    /**
     * Pauses the CPU emulation. If a thread was used for CPU execution
     * and is running, then it should be stopped (destroyed) but the CPU state
     * has to be saved for future run. CPU changes it state to "breakpoint".
     */
    public void pause ();

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.82807136-A8CE-0E76-79FB-B0381B36FD16]
    // </editor-fold> 
    /**
     * Stops the CPU emulation. If a thread was used for CPU execution
     * and is running, then it should be stopped (destroyed) but the CPU state
     * can be saved. CPU changes its state to "stopped" and main module should
     * now forbid execution any of methods <code>step()</code>, <code>pause()</code>,
     * <code>execute()</code> until user resets the CPU. Debug window in main
     * module should be updated with saved CPU state.
     */
    public void stop ();

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.4BFFCA6C-6112-EC77-35F6-11400906D349]
    // </editor-fold> 
    /**
     * Get CPU context. CPU context is an object that implements basic
     * <code>ICPUContext</code> interface. Often this interface is extended
     * by another (not supported by this library), a concrete context for
     * concrete CPU and gives more functionality than basic one. Context is
     * a place, where plugin programmer should implement unsupported, but needed
     * methods and then he should make public its interface. Plugins connected 
     * to CPU get its context as a parameter in plugin connection process, so
     * they can (and should do it in that way) identify the (CPU context)
     * interface and other context information, such as ID or version. After
     * this recognization process plugins recognize (or do not recognize)
     * supported CPU they can be connected with.
     * @return CPU context object
     */
    public ICPUContext getContext ();

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.B43BCF1F-FE11-9C9B-6035-BD984D0A64FC]
    // </editor-fold> 
    /**
     * Gets CPU GUI panel. Each CPU plugin should have GUI panel that shows
     * some important CPU status (e.g. registers, flags, run state, etc.) and
     * allow to user perform some settings (e.g. set the frequency, etc.). This
     * panel is located on right side in panel "emulation" in main module. CPU
     * plugin should update the panel immediately after CPU state changes somehow.
     * @return status GUI panel (instance object)
     */
    public JPanel getStatusGUI ();

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.64C7E8A3-DBAA-2D16-461C-BC839CA8933D]
    // </editor-fold> 
    /**
     * Gets columns in debug window. These columns will be used in the list
     * in the debug window. Usually CPU uses these columns: "breakpoint",
     * "address", "mnemonics" and "opcode". 
     * @return debug columns array
     */
    public IDebugColumn[] getDebugColumns ();

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.F6F9D7E6-A22F-E074-0BCE-5695EB64F8E5]
    // </editor-fold> 
    /**
     * Called when user sets a value to a cell in debug window. This method
     * should ensure proper changes in CPU's internal state, caused by this
     * set. The main module calls this method only if the cell in debug window
     * is editable by user (<code>IDebugColumn.isEditable()</code>).
     * @param row    cell's index from memory position 0 (not row in debug table)
     * @param col    column of the cell in debug window table
     * @param value  new value of the cell
     */
    public void setDebugValue (int row, int col, Object value);

    // <editor-fold defaultstate="collapsed" desc=" UML Marker "> 
    // #[regen=yes,id=DCE.3001AA5A-F7B9-41AF-E4D2-B3A45049FB19]
    // </editor-fold> 
    /**
     * Gets the value of a cell in debug window on specified position. 
     * @param row  cell's index from memory position 0 (not row in debug table)
     * @param col  column of the cell in in debug window table
     * @return value of the cell
     */
    public Object getDebugValue (int row, int col);

}

