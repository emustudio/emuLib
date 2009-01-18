/**
 * ICPUContext.java
 * 
 * (c) Copyright 2008-2009, P. Jakubčo
 * 
 * KISS, YAGNI
 * 
 */
package plugins.cpu;

import java.util.EventListener;
import java.util.EventObject;
import plugins.IContext; 

/**
 * Basic interface for CPU context. The context is used by plugins, that are
 * connected to CPU.
 * 
 * CPU plugins can extend this interface to their own (with some new methods) 
 * and then the programmer should make it to be public in order to other plugins
 * could have access to it.
 * 
 * Extended context may have methods for e.g. connecting devices to CPU, interrupts
 * implementation, etc.
 */
public interface ICPUContext extends IContext {

    /**
     * CPU is stopped (naturally or by user) and should not be run until its
     * reset.
     */
    public static final int STATE_STOPPED_NORMAL       = 1;

    /**
     * CPU is in breakpoint state (paused).
     */
    public static final int STATE_STOPPED_BREAK        = 2;

    /**
     * CPU is stopped because of address fallout error. It should not be
     * run until its reset.
     */
    public static final int STATE_STOPPED_ADDR_FALLOUT = 3;

    /**
     * CPU is stopped because of instruction fallout (unknown instruction) 
     * error. It should not be run until its reset.
     */
    public static final int STATE_STOPPED_BAD_INSTR    = 4;

    /**
     * CPU is running.
     */
    public static final int STATE_RUNNING              = 5;
	
	
    /**
     * Adds the specified CPU listener to receive CPU events from this CPU.
     * CPU events occur when CPU changes its state, or run state. CPU state
     * events don't occur if CPU is running, only happens with run state changes.
     * If listener is <code>null</code>, no exception is thrown and no action is
     * performed.
     * @param listener  the CPU listener
     */
    public void addCPUListener (ICPUContext.ICPUListener listener);

    /**
     * Removes the specified CPU listener so that it no longer receives CPU
     * events from this CPU. CPU events occur when CPU changes its state, or run
     * state. CPU state events don't occur if CPU is running, only happens with
     * run state changes. If listener is <code>null</code>, no exception is 
     * thrown and no action is performed.
     * @param listener  the CPU listener to be removed
     */
    public void removeCPUListener (ICPUContext.ICPUListener listener);

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

        /**
         * Invoked when an CPU's run state changes.
         * @param evt       event object
         * @param runState  new run state of the CPU
         */
        public void runChanged (EventObject evt, int runState);

        /**
         * Invoked when an CPU's state changes. The state can be register change,
         * flags change, or other CPU's internal state change.
         * @param evt  event object
         */
        public void stateUpdated (EventObject evt);

    }

}

