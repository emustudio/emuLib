/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins;

import net.emustudio.emulib.runtime.ApplicationApi;
import net.emustudio.emulib.runtime.settings.PluginSettings;

import java.util.Objects;

/**
 * Base class of every plugin root class. Holds the three constructor arguments every plugin receives.
 */
public abstract class AbstractPlugin implements Plugin {
    protected final long pluginID;
    protected final ApplicationApi applicationApi;
    protected final PluginSettings settings;

    /**
     * Creates new plugin instance.
     *
     * @param pluginID       plugin ID
     * @param applicationApi emuStudio API
     * @param settings       plugin custom settings
     */
    public AbstractPlugin(long pluginID, ApplicationApi applicationApi, PluginSettings settings) {
        this.pluginID = pluginID;
        this.applicationApi = Objects.requireNonNull(applicationApi);
        this.settings = Objects.requireNonNull(settings);
    }
}
