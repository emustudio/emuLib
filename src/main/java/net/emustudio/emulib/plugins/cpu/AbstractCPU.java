/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.cpu;

import net.emustudio.emulib.plugins.annotations.PluginRoot;
import net.emustudio.emulib.runtime.ApplicationApi;
import net.emustudio.emulib.runtime.settings.PluginSettings;

import javax.swing.*;
import java.util.Objects;

/** Plugin conveniences; execution and lifecycle ordering belong to the host. */
@SuppressWarnings("unused")
public abstract class AbstractCPU implements CPU {
    protected final long pluginID;
    protected final ApplicationApi applicationApi;
    protected final PluginSettings settings;
    private boolean destroyed;

    public AbstractCPU(long pluginID, ApplicationApi applicationApi, PluginSettings settings) {
        this.pluginID = pluginID;
        this.applicationApi = Objects.requireNonNull(applicationApi);
        this.settings = Objects.requireNonNull(settings);
    }

    @Override
    public String getTitle() {
        return getClass().getAnnotation(PluginRoot.class).title();
    }

    @Override
    public void showSettings(JFrame parent) { }

    @Override
    public boolean isShowSettingsSupported() {
        return false;
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
