package emulib.plugins;

import java.util.Objects;

public class PluginInitializationException extends Exception {
    private final Plugin plugin;

    public PluginInitializationException(Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin);
    }

    public PluginInitializationException(Plugin plugin, String message) {
        super(message);
        this.plugin = Objects.requireNonNull(plugin);
    }

    public PluginInitializationException(Plugin plugin, String message, Throwable cause) {
        super(message, cause);
        this.plugin = Objects.requireNonNull(plugin);
    }

    public PluginInitializationException(Plugin plugin, Throwable cause) {
        super(cause);
        this.plugin = Objects.requireNonNull(plugin);
    }

    public Plugin getPlugin() {
        return plugin;
    }

}
