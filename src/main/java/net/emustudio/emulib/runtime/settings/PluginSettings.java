/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.settings;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * The class provides methods for reading/writing plugin settings.
 * <p>
 * It is implemented by emuStudio. Plugins obtain this object in the constructor.
 * <p>
 * NOTE: Keys are case-sensitive.
 * <p>
 * Keys which are put into settings must not start with {@link PluginSettings#EMUSTUDIO_PREFIX}. This prefix is
 * reserved for emuStudio settings and might be overridden by emuStudio.
 * However, there are some emuStudio settings obtainable by plugins.
 */
@SuppressWarnings("unused")
public interface PluginSettings extends BasicSettings {

    /**
     * EmuStudio key prefix.
     */
    String EMUSTUDIO_PREFIX = "emustudio.";

    /**
     * Setting key defining if emuStudio runs in "No GUI" mode.
     */
    String EMUSTUDIO_NO_GUI = EMUSTUDIO_PREFIX + "nogui";

    /**
     * Setting key defining if emuStudio runs in "Auto" (non-interactive) mode.
     */
    String EMUSTUDIO_AUTO = EMUSTUDIO_PREFIX + "auto";

    /**
     * "Unavailable" instance of PluginSettings.
     * <p>
     * It means that all methods return dummy or null values.
     * <p>
     * The instance might be useful when creating plugin object without emuStudio (e.g. a plugin wants to support
     * command-line interface).
     */
    PluginSettings UNAVAILABLE = new PluginSettings() {
        /**
         * Returns false.
         * @return false
         */
        @Override
        public boolean contains(String key) {
            return false;
        }

        /**
         * Does nothing.
         */
        @Override
        public void remove(String key) {

        }

        /**
         * Returns Optional.empty().
         * @return Optional.empty()
         */
        @Override
        public Optional<String> getString(String key) {
            return Optional.empty();
        }

        /**
         * Returns default value.
         *
         * @param key          settings key
         * @param defaultValue default value
         * @return defaultValue
         */
        @Override
        public String getString(String key, String defaultValue) {
            return defaultValue;
        }

        /**
         * Returns Optional.empty().
         * @return Optional.empty()
         */
        @Override
        public Optional<Boolean> getBoolean(String key) {
            return Optional.empty();
        }

        /**
         * Returns default value.
         *
         * @param key          settings key
         * @param defaultValue default value
         * @return defaultValue
         */
        @Override
        public boolean getBoolean(String key, boolean defaultValue) {
            return defaultValue;
        }

        /**
         * Returns Optional.empty().
         * @return Optional.empty()
         */
        @Override
        public Optional<Integer> getInt(String key) {
            return Optional.empty();
        }

        /**
         * Returns default value.
         *
         * @param key          settings key
         * @param defaultValue default value
         * @return defaultValue
         */
        @Override
        public int getInt(String key, int defaultValue) {
            return defaultValue;
        }

        /**
         * Returns Optional.empty().
         * @return Optional.empty()
         */
        @Override
        public Optional<Long> getLong(String key) {
            return Optional.empty();
        }

        /**
         * Returns default value.
         *
         * @param key          settings key
         * @param defaultValue default value
         * @return defaultValue
         */
        @Override
        public long getLong(String key, long defaultValue) {
            return defaultValue;
        }

        /**
         * Returns Optional.empty().
         * @return Optional.empty()
         */
        @Override
        public Optional<Double> getDouble(String key) {
            return Optional.empty();
        }

        /**
         * Returns default value.
         *
         * @param key          settings key
         * @param defaultValue default value
         * @return defaultValue
         */
        @Override
        public double getDouble(String key, double defaultValue) {
            return defaultValue;
        }

        /**
         * Returns empty list.
         * @return empty list
         */
        @Override
        public List<String> getArray(String key) {
            return Collections.emptyList();
        }

        /**
         * Returns default value.
         *
         * @param key          settings key
         * @param defaultValue default value
         * @return defaultValue
         */
        @Override
        public List<String> getArray(String key, List<String> defaultValue) {
            return defaultValue;
        }

        /**
         * Does nothing.
         */
        @Override
        public void setString(String key, String value) {

        }

        /**
         * Does nothing.
         */
        @Override
        public void setBoolean(String key, boolean value) {

        }

        /**
         * Does nothing.
         */
        @Override
        public void setInt(String key, int value) {

        }

        /**
         * Does nothing.
         */
        @Override
        public void setLong(String key, long value) {

        }

        /**
         * Does nothing.
         */
        @Override
        public void setDouble(String key, double value) {

        }

        /**
         * Does nothing.
         */
        @Override
        public void setArray(String key, List<String> array) {

        }

        @Override
        public Optional<BasicSettings> getSubSettings(String key) {
            return Optional.empty();
        }

        @Override
        public BasicSettings setSubSettings(String key) throws CannotUpdateSettingException {
            return this;
        }
    };
}
