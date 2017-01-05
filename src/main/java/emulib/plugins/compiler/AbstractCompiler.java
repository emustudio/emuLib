/*
 * KISS, YAGNI, DRY
 *
 * Copyright (C) 2006-2016, Peter Jakubčo
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

package emulib.plugins.compiler;

import emulib.annotations.PluginType;
import emulib.emustudio.SettingsManager;
import emulib.plugins.compiler.Message.MessageType;
import emulib.runtime.exceptions.PluginInitializationException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements some fundamental functionality that can be
 * useful within the implementation of own compiler plug-ins.
 */
public abstract class AbstractCompiler implements Compiler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCompiler.class);

    /**
     * Program start address (memory location)
     */
    protected int programStart = 0; // actualize after compile

    /**
     * Identification number of this plug-in assigned by emuStudio
     */
    protected final long pluginID;

    /**
     * List of all compiler compilerListeners. The compilerListeners are objects implementing
     * the CompilerListener interface. Methods within the compilerListeners are called
     * if the compiler wants to print something out on the screen (info, warning
     * or error message).
     */
    private final Set<CompilerListener> compilerListeners = new CopyOnWriteArraySet<>();

    /**
     * Public constructor initializes compilerListeners list and event object for
     * event passing.
     *
     * @param pluginID ID of the plug-in assigned by emuStudio
     */
    public AbstractCompiler(Long pluginID) {
        this.pluginID = pluginID;
    }

    /**
     * This method semi-initializes the simple compiler. It only
     * set-up data members - pluginID and SettingsManager object.
     *
     * It should be overridden.
     *
     * @param settings settings manipulation object
     * @throws PluginInitializationException never in the default implementation
     */
    @Override
    public void initialize(SettingsManager settings) throws PluginInitializationException {

    }

    @Override
    public final String getTitle() {
        return getClass().getAnnotation(PluginType.class).title();
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
     * Adds a listener onto compilerListeners list
     *
     * @param listener listener object
     * @return true if the listener was added, false otherwise
     */
    @Override
    public boolean addCompilerListener(CompilerListener listener) {
        return compilerListeners.add(listener);
    }

    /**
     * Removes the listener from compilerListeners list
     *
     * @param listener listener object
     * @return true if the listener was removed, false otherwise
     */
    @Override
    public boolean removeCompilerListener(CompilerListener listener) {
        return compilerListeners.remove(listener);
    }

    /**
     * This method notifies all compilerListeners that the compiler is starting
     * the compile process.
     *
     * This method should be called whenever the compiler begins to run.
     */
    protected void notifyCompileStart() {
        compilerListeners.forEach(listener -> {
            try {
                listener.onStart();
            } catch (Exception e) {
                LOGGER.error("Compiler listener error", e);
            }
        });
    }

    /**
     * This method notifies all compilerListeners that the compiler finished
     * the compile process right now.
     *
     * This method should be called whenever the compiler ends the execution.
     *
     * @param errorCode compiler-specific error code
     */
    protected void notifyCompileFinish(int errorCode) {
        compilerListeners.forEach(listener -> {
            try {
                listener.onFinish(errorCode);
            } catch (Exception e) {
                LOGGER.error("Compiler listener error", e);
            }
        });
    }

    /**
     * This method notifies all compilerListeners that the compiler wants to print
     * something out (a message).
     *
     * This method should be called when the compiler wants to notify the user
     * about some warning or error during compilation; or wants to inform the
     * user of something else (e.g. copyright information).
     *
     * @param message The message
     */
    public void notifyOnMessage(Message message) {
        compilerListeners.forEach(listener -> {
            try {
                listener.onMessage(message);
            } catch (Exception e) {
                LOGGER.error("Compiler listener error", e);
            }
        });
    }

    /**
     * Notifies the error message.
     *
     * @param mes text of the message
     */
    public void notifyError(String mes) {
        notifyOnMessage(new Message(MessageType.TYPE_ERROR, mes));
    }

    /**
     * Notifies information message
     *
     * @param mes text of the message
     */
    public void notifyInfo(String mes) {
        notifyOnMessage(new Message(MessageType.TYPE_INFO, mes));
    }

    /**
     * Fires warning message
     *
     * @param mes text of the message
     */
    public void notifyWarning(String mes) {
        notifyOnMessage(new Message(MessageType.TYPE_WARNING, mes));
    }

    /**
     * Does nothing.
     */
    @Override
    public void reset() {

    }
}
