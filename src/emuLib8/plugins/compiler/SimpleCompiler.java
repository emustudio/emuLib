/*
 * SimpleCompiler.java
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

package emuLib8.plugins.compiler;

import java.util.EventObject;
import javax.swing.event.EventListenerList;
import emuLib8.plugins.ISettingsHandler;
import emuLib8.plugins.compiler.Message.MessageType;

/**
 * This class implements some fundamental functionality that can be
 * useful within the implementation of own compiler plug-ins.
 *
 * @author vbmacher
 */
public abstract class SimpleCompiler implements ICompiler {

    /**
     * Settings manipulation object
     */
    protected ISettingsHandler settings;

    /**
     * Program start address (memory location)
     */
    protected int programStart = 0; // actualize after compile

    /**
     * Identification number of this plug-in assigned by emuStudio
     */
    protected long pluginID;

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
    public SimpleCompiler(Long pluginID) {
        changeEvent = new EventObject(this);
        listeners = new EventListenerList();
        this.pluginID = pluginID;
    }

    /**
     * This method semi-initializes the simple compiler. It only
     * set-up data members - pluginID and ISettingsHandler object.
     *
     * It should be overriden.
     *
     * @param sHandler settings manipulation object
     * @return true
     */
    @Override
    public boolean initialize(ISettingsHandler sHandler) {
        this.settings = sHandler;
        return true;
    }

    /**
     * Returns program start address (memory location) after the compilation
     * process. If the compile process was not ran, it will return 0.
     * 
     * @return program start address (memory location)
     */
    @Override
    public int getProgramStartAddress() {
        return programStart;
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
                ((ICompilerListener)listenersList[i+1]).onStart(
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
                ((ICompilerListener)listenersList[i+1]).onFinish(
                        changeEvent, errorCode);
            }
        }
    }

    /**
     * This method notifies all listeners that the compiler wants to print
     * something out (a message).
     *
     * This method should be called when the compiler wants to notify the user
     * about some warning or error during compilation; or wants to inform the
     * user of something else (e.g. copyright information).
     *
     * @param message The message
     */
    public void fireMessage(Message message) {
        Object[] listenersList = listeners.getListenerList();
        for (int i = listenersList.length-2; i>=0; i-=2) {
            if (listenersList[i]==ICompilerListener.class) {
                ((ICompilerListener)listenersList[i+1]).onMessage(changeEvent,
                        message);
            }
        }
    }

    /**
     * Fires the error message.
     * 
     * @param mes text of the message
     */
    protected void printError(String mes) {
        fireMessage(new Message(MessageType.TYPE_ERROR, mes));
    }

    /**
     * Fires information message
     *
     * @param mes text of the message
     */
    protected void printInfo(String mes) {
        fireMessage(new Message(MessageType.TYPE_INFO, mes));
    }

    /**
     * Fires warning message
     *
     * @param mes text of the message
     */
    protected void printWarning(String mes) {
        fireMessage(new Message(MessageType.TYPE_WARNING, mes));
    }

    /**
     * Does nothing.
     */
    @Override
    public void reset() {

    }
}
