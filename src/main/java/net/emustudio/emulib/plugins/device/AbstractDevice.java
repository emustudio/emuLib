// SPDX-License-Identifier: GPL-3.0-or-later
package net.emustudio.emulib.plugins.device;

import net.emustudio.emulib.plugins.PluginInitializationException;
import net.emustudio.emulib.plugins.annotations.PluginRoot;
import net.emustudio.emulib.runtime.ApplicationApi;
import net.emustudio.emulib.runtime.PluginSettings;

import java.util.Objects;

/**
 * Implements fundamental functionality useful for most of the device plugins.
 */
@SuppressWarnings("unused")
public abstract class AbstractDevice implements Device {
    /**
     * Plugin ID assigned by emuStudio
     */
    protected final long pluginID;

    /**
     * emuStudio API.
     */
    protected final ApplicationApi applicationApi;

    /**
     * Device custom settings.
     */
    protected final PluginSettings settings;

    /**
     * Creates new instance.
     *
     * @param pluginID plugin id
     * @param applicationApi emuStudio API
     * @param settings plugin custom settings
     */
    public AbstractDevice(long pluginID, ApplicationApi applicationApi, PluginSettings settings) {
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
}
