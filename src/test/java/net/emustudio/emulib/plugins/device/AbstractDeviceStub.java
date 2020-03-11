// SPDX-License-Identifier: GPL-3.0-or-later
package net.emustudio.emulib.plugins.device;

import net.emustudio.emulib.plugins.annotations.PLUGIN_TYPE;
import net.emustudio.emulib.plugins.annotations.PluginRoot;
import net.emustudio.emulib.runtime.ApplicationApi;
import net.emustudio.emulib.runtime.PluginSettings;

import static org.easymock.EasyMock.createNiceMock;

@PluginRoot(
        type = PLUGIN_TYPE.DEVICE,
        title = "title"
)
public class AbstractDeviceStub extends AbstractDevice {

    public AbstractDeviceStub(long id) {
        super(id, createNiceMock(ApplicationApi.class), createNiceMock(PluginSettings.class));
    }

    @Override
    public void showGUI() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void destroy() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void showSettings() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isShowSettingsSupported() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getCopyright() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getDescription() {
        throw new UnsupportedOperationException();
    }
}
