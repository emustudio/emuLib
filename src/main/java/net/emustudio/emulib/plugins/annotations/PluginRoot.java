// SPDX-License-Identifier: GPL-3.0-or-later
package net.emustudio.emulib.plugins.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the class is a plugin's main class.
 *
 * Besides, the main class must inherit from Plugin interface. This annotation
 * must be used once and only once within the plugin. If this annotation is
 * used more than once (together with the inheritance of Plugin interface), the
 * first occurrence is used (in non-deterministic order).
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PluginRoot {
    String title();
    PLUGIN_TYPE type();
}
