/*
 * CPUAbstract.java
 *
 * (c) Copyright 2010, P.Jakubčo <pjakubco@gmail.com>
 *
 * KISS, YAGNI
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

package plugins.cpu;

import java.util.HashSet;
import plugins.ISettingsHandler;

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
     * breakpoints list
     */
    protected HashSet<Integer> breaks;

    /**
     * thread for the processor execution
     */
    protected Thread cpuThread;
    
    /**
     * ID of this plug-in assigned by emuStudio.
     */
    protected long pluginID;

    /**
     * Run state of this CPU.
     */
    protected int run_state;

    /**
     * Object for settings manipulation.
     */
    protected ISettingsHandler settings;


    /**
     * Public constructor initializes run state and some variables.
     */
    public SimpleCPU() {
        cpuThread = null;
        run_state = ICPU.STATE_STOPPED_NORMAL;
        breaks = new HashSet<Integer>();
    }

    /**
     * This method initializes the CPU. It stores pluginID and settings into
     * variables.
     *
     * @param pluginID ID of this plug-in assigned by emuStudio
     * @param settings object for settings manipulation
     * @return true
     */
    @Override
    public boolean initialize(long pluginID, ISettingsHandler settings) {
        this.pluginID = pluginID;
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
     * Creates and executes the cpuThread thread. The CPU sets itself to
     * the running state. In the thread, don't forget to call context.fireCpuRun
     * method.
     *
     * This method also changes the run_state variable to STATE_RUNNING.
     */
    @Override
    public void execute() {
        run_state = ICPU.STATE_RUNNING;

        cpuThread = new Thread(this, getTitle());
        cpuThread.start();
    }

}
