// SPDX-License-Identifier: GPL-3.0-or-later
package net.emustudio.emulib.plugins.memory;

import net.emustudio.emulib.plugins.annotations.PLUGIN_TYPE;
import net.emustudio.emulib.plugins.annotations.PluginRoot;
import net.emustudio.emulib.runtime.ApplicationApi;
import net.emustudio.emulib.runtime.PluginSettings;
import static org.easymock.EasyMock.createNiceMock;

@PluginRoot(
        type = PLUGIN_TYPE.MEMORY,
        title = "title"
)
public class AbstractMemoryStub extends AbstractMemory {

    public AbstractMemoryStub(long id) {
        super(id, createNiceMock(ApplicationApi.class), createNiceMock(PluginSettings.class));
    }

    @Override
    public int getSize() {
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