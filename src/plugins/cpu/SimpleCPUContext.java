/*
 * CPUContextAbstract.java
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

import java.util.EventObject;
import javax.swing.event.EventListenerList;

/**
 * This class implements fundamental functionality of CPU context that
 * can be used by CPU plug-ins.
 *
 * Actually, it implements the storage of CPU listeners.
 *
 * @author vbmacher
 */
public abstract class SimpleCPUContext implements ICPUContext {
    /**
     * List of all CPU listeners
     */
    protected EventListenerList listenerList;

    /**
     * Event object
     */
    private EventObject cpuEvt;

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
     * called by the CPU when it changes the run state. Don't forget to call
     * it in the CPU implementation.
     *
     * @param run_state  new processor state
     */
    public void fireCpuRun(int run_state) {
        Object[] listeners = listenerList.getListenerList();
        for (int i=0; i<listeners.length; i+=2) {
            if (listeners[i] == ICPUListener.class)
                ((ICPUListener)listeners[i+1]).runChanged(cpuEvt, run_state);
        }
    }

    /**
     * This method should be called by the CPU, when it changes internal state,
     * like register values or flags change. It then fires up all the listeners
     * (stateUpdated method). Don't forget to call it in the CPU implementation
     * on the proper place.
     */
    public void fireCpuState() {
        Object[] listeners = listenerList.getListenerList();
        for (int i=0; i<listeners.length; i+=2) {
            if (listeners[i] == ICPUListener.class)
                ((ICPUListener)listeners[i+1]).stateUpdated(cpuEvt);
        }
    }

}
