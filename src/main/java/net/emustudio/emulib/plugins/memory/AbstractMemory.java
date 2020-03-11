// SPDX-License-Identifier: GPL-3.0-or-later
package net.emustudio.emulib.plugins.memory;

import net.emustudio.emulib.plugins.PluginInitializationException;
import net.emustudio.emulib.plugins.annotations.PluginRoot;
import net.emustudio.emulib.runtime.ApplicationApi;
import net.emustudio.emulib.runtime.PluginSettings;

import java.util.Objects;

/**
 * Implements fundamental functionality useful for most of the memory plugins.
 */
public abstract class AbstractMemory implements Memory {
    /**
     * Loaded program location in memory. This variable is changed by compiler (mostly).
     */
    private int programLocation;

    /**
     * Plugin ID assigned by emuStudio
     */
    protected final long pluginID;

    /**
     * emuStudio API.
     */
    protected final ApplicationApi applicationApi;

    /**
     * Memory custom settings.
     */
    protected final PluginSettings settings;

    /**
     * Creates new instance.
     *
     * @param pluginID plugin ID
     * @param applicationApi emuStudio API
     * @param settings plugin custom settings
     */
    public AbstractMemory(long pluginID, ApplicationApi applicationApi, PluginSettings settings) {
        this.pluginID = pluginID;
        this.applicationApi = Objects.requireNonNull(applicationApi);
        this.settings = Objects.requireNonNull(settings);
    }

    /**
     * No-operation. Should be overridden if needed.
     */
    @Override
    public void initialize() throws PluginInitializationException {

    }

    /**
     * Get program location in memory.
     *
     * @return program memory location
     */
    @Override
    public int getProgramLocation() {
        return programLocation;
    }

    /**
     * Set program location in memory.
     *
     * It should be called after successful compilation, if the compiler has loaded the program in the memory.
     * Program location will be then used by CPU and emuStudio to know where it can start emulating the program.
     *
     * @param programLocation program location in memory
     */
    @Override
    public void setProgramLocation(int programLocation) {
        this.programLocation = programLocation;
    }

    @Override
    public String getTitle() {
        return getClass().getAnnotation(PluginRoot.class).title();
    }

    /**
     * No-operation. Should be overridden if needed.
     */
    @Override
    public void reset() {

    }
}
