/*
 * SimpleCompilerContext.java
 *
 * KISS, YAGNI
 *
 * Copyright (C) 2010 Peter Jakubčo <pjakubco at gmail.com>
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

package plugins.compiler;

import java.util.EventObject;
import javax.swing.event.EventListenerList;

/**
 *
 * @author vbmacher
 */
public abstract class SimpleCompilerContext implements ICompilerContext {

    /**
     * List of all compiler listeners. The listeners are objects implementing
     * the ICompilerListener interface. Methods within the listeners are called
     * if the compiler wants to print something out on the screen (info, warning
     * or error message).
     */
    protected EventListenerList listeners;

    /**
     * Event object
     */
    protected EventObject changeEvent;

    /**
     * Public constructor initializes listeners list and event object for
     * event passing.
     */
    public SimpleCompilerContext() {
        changeEvent = new EventObject(this);
        listeners = new EventListenerList();
    }

    /**
     * Adds a listener onto listeners list
     *
     * @param listener listener object
     */
    @Override
    public void addCompilerListener(ICompilerListener listener) {
        listeners.add(ICompilerListener.class, listener);
    }

    /**
     * Removes the listener from listeners list
     *
     * @param listener listener object
     */
    @Override
    public void removeCompilerListener(ICompilerListener listener) {
        listeners.remove(ICompilerListener.class, listener);
    }

    /**
     * This method notifies all listeners that the compiler is starting
     * the compile process.
     *
     * This method should be called whenever the compiler begins to run.
     */
    public void fireCompileStart() {
        Object[] listenersList = listeners.getListenerList();
        for (int i = listenersList.length-2; i>=0; i-=2) {
            if (listenersList[i]==ICompilerListener.class) {
                ((ICompilerListener)listenersList[i+1]).onCompileStart(
                        changeEvent);
            }
        }
    }

    /**
     * This method notifies all listeners that the compiler finished
     * the compile process right now.
     *
     * This method should be called whenever the compiler ends the execution.
     *
     * @param errorCode compiler-specific error code
     */
    public void fireCompileFinish(int errorCode) {
        Object[] listenersList = listeners.getListenerList();
        for (int i = listenersList.length-2; i>=0; i-=2) {
            if (listenersList[i]==ICompilerListener.class) {
                ((ICompilerListener)listenersList[i+1]).onCompileFinish(
                        changeEvent, errorCode);
            }
        }
    }

    /**
     * This method notifies all listeners that the compiler wants to print
     * something out
     *
     * This method should be called when the compiler notifies some warning
     * or error, or wants to inform the user of something (e.g. copyright
     * information).
     *
     * @param row line number according to source code
     * @param col column number according to souce code
     * @param message the message
     * @param errorCode error code, compiler-specific
     * @param messageType type of the message (TYPE_INFO, TYPE_WARNING, TYPE_ERROR)
     */
    public void fireMessage(int row, int col, String message, int errorCode,
            int messageType) {
        Object[] listenersList = listeners.getListenerList();
        for (int i = listenersList.length-2; i>=0; i-=2) {
            if (listenersList[i]==ICompilerListener.class) {
                ((ICompilerListener)listenersList[i+1]).onCompileInfo(
                        changeEvent, row, col, message, errorCode, messageType);
            }
        }
    }

}
