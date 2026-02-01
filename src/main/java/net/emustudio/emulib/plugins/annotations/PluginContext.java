/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.annotations;

import java.lang.annotation.*;

/**
 * Indicates that the annotated interface represent plugin context.
 * <p>
 * This annotation should be used only on classes.
 */
@SuppressWarnings("unused")
@Inherited
@Retention(value = RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PluginContext {
    String id() default "unknown";
}
