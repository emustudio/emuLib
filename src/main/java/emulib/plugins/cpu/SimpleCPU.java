/*
 * CPUAbstract.java
 *
 * KISS, YAGNI, DRY
 * 
 * (c) Copyright 2010-2012, Peter Jakubčo
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

package emulib.plugins.cpu;

import java.util.EventObject;
import java.util.HashSet;
import javax.swing.event.EventListenerList;
import emulib.plugins.ISettingsHandler;

/**
 * This class implements some fundamental functionality that can be used
 * by your own plug-ins.
 *
 * The CPU execution is realized via separated thread.
 *
 * @author vbmacher
 */
public abstract class SimpleCPU implements ICPU, Runnable {
    /**
     * List of all CPU listeners
     */
    protected EventListenerList listenerList;

    /**
     * Event object
     */
    protected EventObject cpuEvt;

    /**
     * breakpoints list
     */
    protected HashSet<Integer> breaks;

    /**
     * ID of this plug-in assigned by emuStudio.
     */
    protected long pluginID;

    /**
     * Run state of this CPU.
     */
    protected RunState run_state;

    /**
     * Object for settings manipulation.
     */
    protected ISettingsHandler settings;

    /**
     * This thread object. It is used for the CPU execution.
     */
    protected Thread cpuThread;


    /**
     * Public constructor initializes run state and some variables.
     *
     * @param pluginID plug-in identification number
     */
    public SimpleCPU(Long pluginID) {
        run_state = RunState.STATE_STOPPED_NORMAL;
        breaks = new HashSet<Integer>();
        listenerList = new EventListenerList();
        cpuEvt = new EventObject(this);
        this.pluginID = pluginID;
        cpuThread = null;
    }

    /**
     * This method initializes the CPU. It stores pluginID and settings into
     * variables.
     *
     * @param settings object for settings manipulation
     * @return true
     */
    @Override
    public boolean initialize(ISettingsHandler settings) {
        this.settings = settings;
        return true;
    }

    /**
     * This class support breakpoints.
     *
     * @return true
     */
    @Override
    public boolean isBreakpointSupported() {
        return true;
    }

    /**
     * Set breakpoint on given memory location
     * .
     * @param pos memory location
     * @param set true for set, false for unset
     */
    @Override
    public void setBreakpoint(int pos, boolean set) {
        if (set) breaks.add(pos);
        else breaks.remove(pos);
    }

    /**
     * Get breakpoint on specified memory location
     *
     * @param pos memory location
     * @return true if breakpoint is set, false otherwise
     */
    @Override
    public boolean getBreakpoint(int pos) {
        return breaks.contains(pos);
    }

    /**
     * This method resets the CPU by calling overriden method reset(int).
     */
    @Override
    public void reset() { reset(0); }

    /**
     * Sets the run_state to STATE_STOPPED_BREAK and nulls the thread
     * object. Should be overriden.
     *
     * @param addr memory location where to begin the emulation
     */
    @Override
    public void reset(int addr) {
        run_state = RunState.STATE_STOPPED_BREAK;
        cpuThread = null;
    }

    /**
     * Add new CPU listener to the list of listeners. CPU listener is an
     * implementation object of ICPUListener interface. The methods are
     * called when some events are occured on CPU.
     *
     * @param listener ICPUListener object
     */
    @Override
    public void addCPUListener(ICPUListener listener) {
        listenerList.add(ICPUListener.class, listener);
    }

    /**
     * Remove CPU listener object from the list of listeners. If the listener
     * is not included in the list, nothing will be done.
     *
     * @param listener ICPUListener object
     */
    @Override
    public void removeCPUListener(ICPUListener listener) {
        listenerList.remove(ICPUListener.class, listener);
    }

    /**
     * This method fires up all listeners (runChanged method). It should be
     * called by the CPU when it changes the run state.
     *
     * @param run_state  new processor state
     */
    public void fireCpuRun(RunState run_state) {
        Object[] listeners = listenerList.getListenerList();
        for (int i=0; i<listeners.length; i+=2) {
            if (listeners[i] == ICPUListener.class)
                ((ICPUListener)listeners[i+1]).runChanged(cpuEvt, run_state);
        }
    }

    /**
     * This method should be called by the CPU, when it changes internal state,
     * like register values or flags change. It then fires up all the listeners
     * (stateUpdated method).
     */
    public void fireCpuState() {
        Object[] listeners = listenerList.getListenerList();
        for (int i=0; i<listeners.length; i+=2) {
            if (listeners[i] == ICPUListener.class)
                ((ICPUListener)listeners[i+1]).stateUpdated(cpuEvt);
        }
    }

    /**
     * Creates and starts new thread of this class.
     */
    @Override
    public void execute() {
        cpuThread = new Thread(this, getTitle());
        cpuThread.start();
    }

}
