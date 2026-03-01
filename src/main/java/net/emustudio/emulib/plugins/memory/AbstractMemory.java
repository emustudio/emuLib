/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.memory;

import net.emustudio.emulib.plugins.PluginInitializationException;
import net.emustudio.emulib.plugins.annotations.PluginRoot;
import net.emustudio.emulib.plugins.memory.annotations.Annotations;
import net.emustudio.emulib.plugins.memory.annotations.MemoryAnnotations;
import net.emustudio.emulib.runtime.ApplicationApi;
import net.emustudio.emulib.runtime.settings.PluginSettings;

import java.util.Objects;

/**
 * Implements fundamental functionality useful for most of the memory plugins.
 */
@SuppressWarnings("unused")
public abstract class AbstractMemory implements Memory {
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
     * Memory annotations.
     */
    protected final MemoryAnnotations annotations = new Annotations();

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

    @Override
    public MemoryAnnotations getAnnotations() {
        return annotations;
    }
}
