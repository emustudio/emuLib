// SPDX-License-Identifier: GPL-3.0-or-later
package net.emustudio.emulib.plugins.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated interface represent plugin context.
 *
 * This annotation should be used only on classes.
 */
@SuppressWarnings("unused")
@Inherited
@Retention(value = RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PluginContext {
    String id() default "unknown";
}
