package dependencies;

import net.emustudio.emulib.annotations.EMULIB_VERSION;
import net.emustudio.emulib.annotations.PLUGIN_TYPE;
import net.emustudio.emulib.annotations.PluginType;
import net.emustudio.emulib.emustudio.SettingsManager;
import net.emustudio.emulib.plugins.Plugin;
import net.emustudio.emulib.runtime.exceptions.PluginInitializationException;
import net.emustudio.emulib.runtime.ContextPool;
import dependencies.hidden.BdependsOnC;

@PluginType(
        title = "A",
        description = "A",
        copyright = "A",
        type = PLUGIN_TYPE.COMPILER,
        emuLibVersion = EMULIB_VERSION.VERSION_9
)
public class APluginDependsOnB implements Plugin {
    private final BdependsOnC b = new BdependsOnC();

    public APluginDependsOnB(Long pid, ContextPool contextPool) {

    }

    public void hi() {
        b.hi();
    }

    @Override
    public void reset() {

    }

    @Override
    public void initialize(SettingsManager settingsManager) throws PluginInitializationException {

    }

    @Override
    public void destroy() {

    }

    @Override
    public void showSettings() {

    }

    @Override
    public boolean isShowSettingsSupported() {
        return false;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getVersion() {
        return null;
    }
}
