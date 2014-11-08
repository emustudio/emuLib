/*
 * KISS, YAGNI, DRY
 *
 * (c) Copyright 2010-2014, Peter Jakubčo
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

import emulib.annotations.PluginType;
import emulib.emustudio.SettingsManager;
import emulib.plugins.PluginInitializationException;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This class implements some fundamental functionality that can be used
 * by your own plug-ins.
 *
 * The CPU execution is realized via separated thread.
 */
@ThreadSafe
public abstract class AbstractCPU implements CPU, Runnable {
    /**
     * List of all CPU stateObservers
     */
    protected final List<CPUListener> stateObservers = new CopyOnWriteArrayList<>();

    /**
     * breakpoints list
     */
    protected final Set<Integer> breakpoints = new ConcurrentSkipListSet<>();

    /**
     * ID of this plug-in assigned by emuStudio.
     */
    protected final long pluginID;

    /**
     * Run state of this CPU.
     */
    @GuardedBy("this")
    private RunState runState = RunState.STATE_STOPPED_NORMAL;

    /**
     * Object for settings manipulation.
     */
    protected volatile SettingsManager settings;

    /**
     * This thread object. It is used for the CPU execution.
     */
    @GuardedBy("this")
    private Thread cpuThread;


    /**
     * Public constructor initializes run state and some variables.
     *
     * @param pluginID plug-in identification number
     */
    public AbstractCPU(Long pluginID) {
        this.pluginID = pluginID;
    }

    /**
     * This method initializes the CPU. It stores pluginID and settings into
     * variables.
     *
     * @param settings object for settings manipulation
     */
    @Override
    public void initialize(SettingsManager settings) throws PluginInitializationException {
        this.settings = settings;
    }

    @Override
    public String getTitle() {
        return getClass().getAnnotation(PluginType.class).title();
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

    @Override
    public void setBreakpoint(int memLocation) {
        breakpoints.add(memLocation);
    }

    @Override
    public void unsetBreakpoint(int memLocation) {
        breakpoints.remove(memLocation);
    }

    @Override
    public boolean isBreakpointSet(int memLocation) {
        return breakpoints.contains(memLocation);
    }

    protected synchronized void setRunState(RunState tmpRunState) {
        this.runState = tmpRunState;
        if (tmpRunState != RunState.STATE_RUNNING) {
            if (cpuThread != null) {
                try {
                    cpuThread.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                cpuThread = null;
            }
        } else if (tmpRunState == RunState.STATE_RUNNING) {
            if (cpuThread == null) {
                cpuThread = new Thread(this);
                cpuThread.start();
            }
        }
        notifyStateChanged(runState);
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
        setRunState(RunState.STATE_STOPPED_BREAK);
    }

    /**
     * Add new CPU listener to the list of stateObservers. CPU listener is an
     * implementation object of CPUListener interface. The methods are
     * called when some events are occured on CPU.
     *
     * @param listener CPUListener object
     * @return true if the listener was added, false otherwise
     */
    @Override
    public boolean addCPUListener(CPUListener listener) {
        return stateObservers.add(listener);
    }

    /**
     * Remove CPU listener object from the list of stateObservers. If the listener
     * is not included in the list, nothing will be done.
     *
     * @param listener CPUListener object
     * @return true if the listener was return, false otherwise
     */
    @Override
    public boolean removeCPUListener(CPUListener listener) {
        return stateObservers.remove(listener);
    }

    private void notifyStateChanged(RunState runState) {
        for (CPUListener observer : stateObservers) {
            observer.runStateChanged(runState);
            observer.internalStateChanged();
        }
    }

    /**
     * Creates and starts new thread of this class.
     */
    @Override
    public void execute() {
        setRunState(RunState.STATE_RUNNING);
    }

}
