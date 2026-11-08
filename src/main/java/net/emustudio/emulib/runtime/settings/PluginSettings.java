/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.settings;

import java.util.List;
import java.util.Optional;

/**
 * Plugin settings (hierarchical key-value store).
 * <p>
 * It is implemented by emuStudio. Plugins obtain this object in the constructor.
 * Getters return empty Optional if the key is missing or the value has a different type.
 * Use {@code orElse(default)} for default values. Keys are case-sensitive.
 * <p>
 * Keys must not start with {@link #EMUSTUDIO_PREFIX}. This prefix is reserved for emuStudio settings, some of which
 * are readable by plugins.
 */
public interface PluginSettings {
    String EMUSTUDIO_PREFIX = "emustudio.";

    /**
     * Boolean: emuStudio runs without GUI.
     */
    String EMUSTUDIO_NO_GUI = EMUSTUDIO_PREFIX + "nogui";

    /**
     * Boolean: emuStudio runs automatic emulation.
     */
    String EMUSTUDIO_AUTO = EMUSTUDIO_PREFIX + "auto";

    /**
     * Settings which contain nothing and ignore updates. Used when emuStudio is not available (e.g. in tests).
     */
    PluginSettings UNAVAILABLE = new PluginSettings() {
        public Optional<String> getString(String key) { return Optional.empty(); }
        public Optional<Boolean> getBoolean(String key) { return Optional.empty(); }
        public Optional<Integer> getInt(String key) { return Optional.empty(); }
        public Optional<Long> getLong(String key) { return Optional.empty(); }
        public Optional<Double> getDouble(String key) { return Optional.empty(); }
        public List<String> getArray(String key) { return List.of(); }
        public Optional<PluginSettings> getSubSettings(String key) { return Optional.empty(); }
        public PluginSettings setSubSettings(String key) { return this; }
        public void set(String key, Object value) { }
    };

    Optional<String> getString(String key);

    Optional<Boolean> getBoolean(String key);

    Optional<Integer> getInt(String key);

    Optional<Long> getLong(String key);

    Optional<Double> getDouble(String key);

    /**
     * @param key setting key
     * @return list of strings, empty if the key is missing
     */
    List<String> getArray(String key);

    /**
     * @param key setting key
     * @return nested settings, if present
     */
    Optional<PluginSettings> getSubSettings(String key);

    /**
     * Creates (or replaces) nested settings.
     *
     * @param key setting key
     * @return new empty nested settings
     * @throws CannotUpdateSettingException if the settings cannot be updated
     */
    PluginSettings setSubSettings(String key);

    /**
     * Sets or removes a setting.
     *
     * @param key   setting key
     * @param value String, Boolean, Integer, Long, Double, or List of Strings; null removes the setting
     * @throws CannotUpdateSettingException if the settings cannot be updated
     * @throws IllegalArgumentException     if the value type is not supported
     */
    void set(String key, Object value);
}
