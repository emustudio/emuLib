/*
 * KISS, YAGNI, DRY
 *
 * (c) Copyright 2006-2016, Peter Jakubčo
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package emulib.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the class is a plug-in's main class.
 *
 * Besides, the main class must inherit from Plugin interface. This annotation
 * must be used once and only once within the plug-in. If this annotation is
 * used more than once (together with the inheritance of Plugin interface), the
 * first occurence is used.
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PluginType {
    String title();
    String copyright();
    String description();
    PLUGIN_TYPE type();
    EMULIB_VERSION emuLibVersion() default EMULIB_VERSION.VERSION_9;
}
