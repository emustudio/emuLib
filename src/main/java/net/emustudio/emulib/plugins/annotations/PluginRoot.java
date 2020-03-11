/*
 * Run-time library for emuStudio and plugins.
 *
 *     Copyright (C) 2006-2020  Peter Jakubčo
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
