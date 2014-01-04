package emulib.plugins.memory;

import emulib.annotations.PLUGIN_TYPE;
import emulib.annotations.PluginType;

@PluginType(
        type = PLUGIN_TYPE.MEMORY,
        title = "title",
        copyright = "copyright",
        description = "description"
)
public class AbstractMemoryStub extends AbstractMemory {

    public AbstractMemoryStub(Long id) {
        super(id);
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

}
