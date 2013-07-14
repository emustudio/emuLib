package emulib.runtime;

/**
 * This exception will be raised when {@link emulib.runtime.PluginLoader#resolveLoadedClasses() PluginLoader.resolveLoadedClasses}
 * method fails.
 *
 * @author jakubco
 */
public class PluginNotFullyLoadedException extends Exception {

    public PluginNotFullyLoadedException(String message) {
        super(message);
    }

}
