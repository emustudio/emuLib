package net.emustudio.emulib.runtime.settings;

import java.util.List;
import java.util.Optional;

/**
 * Basic settings of a plugin.
 */
@SuppressWarnings("unused")
public interface BasicSettings {

    /**
     * Check if a key is present in settings.
     *
     * @param key setting key
     * @return true if key is present in settings
     */
    boolean contains(String key);

    /**
     * Remove key from settings if present.
     * <p>
     * NOTE: If key does not exist already, nothing will happen.
     *
     * @param key settings key
     * @throws CannotUpdateSettingException if the setting could not be removed
     */
    void remove(String key) throws CannotUpdateSettingException;

    /**
     * Get a String value from settings.
     *
     * @param key settings key
     * @return String value if present
     */
    Optional<String> getString(String key);

    /**
     * Get a String value from settings.
     *
     * @param key          settings key
     * @param defaultValue default value
     * @return String value if present, or defaultValue if not
     */
    String getString(String key, String defaultValue);

    /**
     * Get a boolean value from settings.
     *
     * @param key settings key
     * @return boolean value if present
     */
    Optional<Boolean> getBoolean(String key);

    /**
     * Get a boolean value from settings.
     *
     * @param key          settings key
     * @param defaultValue default value
     * @return boolean value if present, or defaultValue if not
     */
    boolean getBoolean(String key, boolean defaultValue);

    /**
     * Get an int value from settings.
     *
     * @param key settings key
     * @return int value if present
     */
    Optional<Integer> getInt(String key);

    /**
     * Get an int value from settings.
     *
     * @param key          settings key
     * @param defaultValue default value
     * @return int value if present, or defaultValue if not
     */
    int getInt(String key, int defaultValue);

    /**
     * Get a long value from settings.
     *
     * @param key settings key
     * @return long value if present
     */
    Optional<Long> getLong(String key);

    /**
     * Get a long value from settings.
     *
     * @param key          settings key
     * @param defaultValue default value
     * @return long value if present, or defaultValue if not
     */
    long getLong(String key, long defaultValue);

    /**
     * Get a double value from settings.
     *
     * @param key settings key
     * @return double value if present
     */
    Optional<Double> getDouble(String key);

    /**
     * Get a double value from settings.
     *
     * @param key          settings key
     * @param defaultValue default value
     * @return double value if present, or defaultValue if not
     */
    double getDouble(String key, double defaultValue);

    /**
     * Get an array from settings.
     *
     * @param key settings key
     * @return List of String values if the key is present; empty List otherwise
     */
    List<String> getArray(String key);

    /**
     * Get an array from settings.
     *
     * @param key          settings key
     * @param defaultValue default value
     * @return List of String values if the key is present; default value otherwise
     */
    List<String> getArray(String key, List<String> defaultValue);


    /**
     * Set a String value to the settings.
     *
     * @param key   settings key
     * @param value the value
     * @throws CannotUpdateSettingException if the setting could not be set
     */
    void setString(String key, String value) throws CannotUpdateSettingException;

    /**
     * Set a boolean value to the settings.
     *
     * @param key   settings key
     * @param value the value
     * @throws CannotUpdateSettingException if the setting could not be set
     */
    void setBoolean(String key, boolean value) throws CannotUpdateSettingException;

    /**
     * Set an int value to the settings.
     *
     * @param key   settings key
     * @param value the value
     * @throws CannotUpdateSettingException if the setting could not be set
     */
    void setInt(String key, int value) throws CannotUpdateSettingException;

    /**
     * Set a long value to the settings.
     *
     * @param key   settings key
     * @param value the value
     * @throws CannotUpdateSettingException if the setting could not be set
     */
    void setLong(String key, long value) throws CannotUpdateSettingException;

    /**
     * Set a double value to the settings.
     *
     * @param key   settings key
     * @param value the value
     * @throws CannotUpdateSettingException if the setting could not be set
     */
    void setDouble(String key, double value) throws CannotUpdateSettingException;

    /**
     * Set an array to the settings.
     *
     * @param key   settings key
     * @param array the value
     * @throws CannotUpdateSettingException if the setting could not be set
     */
    void setArray(String key, List<String> array) throws CannotUpdateSettingException;

    /**
     * Get a sub-settings if exists.
     *
     * @param key settings key
     * @return sub-settings for that key if it exists
     */
    Optional<BasicSettings> getSubSettings(String key);

    /**
     * Set new sub-settings to the settings.
     * Overrides existing with empty one.
     *
     * @param key settings key
     * @return new sub-settings
     * @throws CannotUpdateSettingException if the sub-settings could not be set
     */
    BasicSettings setSubSettings(String key) throws CannotUpdateSettingException;

}
