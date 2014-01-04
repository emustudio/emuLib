package emulib.plugins.device;

import emulib.annotations.PLUGIN_TYPE;
import emulib.annotations.PluginType;

@PluginType(
        type = PLUGIN_TYPE.DEVICE,
        title = "title",
        copyright = "copyright",
        description = "description"
)
public class AbstractDeviceStub extends AbstractDevice {

    public AbstractDeviceStub(Long id) {
        super(id);
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

}
