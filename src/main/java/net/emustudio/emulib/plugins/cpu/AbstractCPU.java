/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.cpu;

import net.emustudio.emulib.plugins.AbstractPlugin;
import net.emustudio.emulib.runtime.ApplicationApi;
import net.emustudio.emulib.runtime.settings.PluginSettings;

/** Plugin conveniences; execution and lifecycle ordering belong to the host. */
public abstract class AbstractCPU extends AbstractPlugin implements CPU {
    private boolean destroyed;

    /**
     * Creates new instance of CPU.
     *
     * @param pluginID       plugin ID
     * @param applicationApi emuStudio API
     * @param settings       plugin custom settings
     */
    public AbstractCPU(long pluginID, ApplicationApi applicationApi, PluginSettings settings) {
        super(pluginID, applicationApi, settings);
    }

    @Override
    public final void reset() {
        reset(applicationApi.getProgramLocation());
    }

    /** Host calls only after actual execution exit. No stop request or thread waiting occurs here. */
    @Override
    public final void destroy() {
        if (destroyed) return;
        destroyed = true;
        destroyInternal();
    }

    protected abstract void destroyInternal();
}
