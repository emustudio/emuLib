/*
 * Run-time library for emuStudio and plugins.
 *
 *     Copyright (C) 2006-2020  Peter Jakubčo
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.emustudio.emulib.plugins.compiler;

import net.emustudio.emulib.plugins.PluginInitializationException;
import net.emustudio.emulib.plugins.annotations.PluginRoot;
import net.emustudio.emulib.plugins.compiler.CompilerMessage.MessageType;
import net.emustudio.emulib.runtime.ApplicationApi;
import net.emustudio.emulib.runtime.PluginSettings;
import net.jcip.annotations.NotThreadSafe;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Implements fundamental functionality useful for most of the compiler plugins.
 */
@SuppressWarnings("unused")
@NotThreadSafe
public abstract class AbstractCompiler implements Compiler {
    /**
     * Plugin ID assigned by emuStudio
     */
    protected final long pluginID;

    /**
     * emuStudio API.
     */
    protected final ApplicationApi applicationApi;

    /**
     * Compiler custom settings.
     */
    protected final PluginSettings settings;

    /**
     * List of all compiler compilerListeners. The compilerListeners are objects implementing
     * the CompilerListener interface. Methods within the compilerListeners are called
     * if the compiler wants to print something out on the screen (info, warning
     * or error message).
     */
    private final Set<CompilerListener> compilerListeners = new CopyOnWriteArraySet<>();

    /**
     * Creates new instance.
     *
     * @param pluginID       plugin ID
     * @param applicationApi emuStudio API
     * @param settings       plugin custom settings
     */
    public AbstractCompiler(long pluginID, ApplicationApi applicationApi, PluginSettings settings) {
        this.pluginID = pluginID;
        this.applicationApi = Objects.requireNonNull(applicationApi);
        this.settings = Objects.requireNonNull(settings);
    }

    /**
     * Does nothing. Should be overriden.
     */
    @Override
    public void initialize() throws PluginInitializationException {

    }

    /**
     * Does nothing. Should be overriden.
     */
    @Override
    public void destroy() {

    }

    /**
     * Get plugin title
     * @return title from the {@link PluginRoot} annotation
     */
    @Override
    public String getTitle() {
        return getClass().getAnnotation(PluginRoot.class).title();
    }

    /**
     * Does nothing. Should be overriden.
     */
    @Override
    public void showSettings() {

    }

    /**
     * Return false. Should be overriden.
     * @return false
     */
    @Override
    public boolean isShowSettingsSupported() {
        return false;
    }

    /**
     * Adds a listener onto compilerListeners list
     *
     * @param listener listener object
     */
    @Override
    public void addCompilerListener(CompilerListener listener) {
        compilerListeners.add(listener);
    }

    /**
     * Removes the listener from compilerListeners list
     *
     * @param listener listener object
     */
    @Override
    public void removeCompilerListener(CompilerListener listener) {
        compilerListeners.remove(listener);
    }

    /**
     * This method notifies all compilerListeners that the compiler is starting
     * the compile process.
     * <p>
     * This method should be called whenever the compiler begins to run.
     */
    protected void notifyCompileStart() {
        compilerListeners.forEach(CompilerListener::onStart);
    }

    /**
     * This method notifies all compilerListeners that the compiler finished
     * the compile process right now.
     * <p>
     * This method should be called whenever the compiler ends the execution.
     */
    protected void notifyCompileFinish() {
        compilerListeners.forEach(CompilerListener::onFinish);
    }

    /**
     * This method notifies all compilerListeners that the compiler wants to print
     * something out (a message).
     * <p>
     * This method should be called when the compiler wants to notify the user
     * about some warning or error during compilation; or wants to inform the
     * user of something else (e.g. copyright information).
     *
     * @param compilerMessage The message
     */
    public void notifyOnMessage(CompilerMessage compilerMessage) {
        compilerListeners.forEach(listener -> listener.onMessage(compilerMessage));
    }

    /**
     * Notifies the error message.
     *
     * @param msg text of the message
     */
    public void notifyError(String msg) {
        notifyOnMessage(new CompilerMessage(MessageType.TYPE_ERROR, msg));
    }

    /**
     * Notifies information message
     *
     * @param msg text of the message
     */
    public void notifyInfo(String msg) {
        notifyOnMessage(new CompilerMessage(MessageType.TYPE_INFO, msg));
    }

    /**
     * Fires warning message
     *
     * @param msg text of the message
     */
    public void notifyWarning(String msg) {
        notifyOnMessage(new CompilerMessage(MessageType.TYPE_WARNING, msg));
    }

    /**
     * Does nothing.
     */
    @Override
    public void reset() {

    }
}
