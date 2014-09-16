package emulib.plugins;

public class PluginInitializationException extends Exception {
    private final Plugin plugin;

    public PluginInitializationException(Plugin plugin) {
        this.plugin = plugin;
    }

    public PluginInitializationException(Plugin plugin, String message) {
        super(message);
        this.plugin = plugin;
    }

    public PluginInitializationException(Plugin plugin, String message, Throwable cause) {
        super(message, cause);
        this.plugin = plugin;
    }

    public PluginInitializationException(Plugin plugin, Throwable cause) {
        super(cause);
        this.plugin = plugin;
    }

    public PluginInitializationException(Plugin plugin, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.plugin = plugin;
    }

    public Plugin getPlugin() {
        return plugin;
    }

}
