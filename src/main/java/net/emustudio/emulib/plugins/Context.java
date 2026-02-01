/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins;

import net.emustudio.emulib.plugins.annotations.PluginContext;
import net.jcip.annotations.ThreadSafe;

/**
 * A plugin context is a runtime structure implemented and used by plugins in order to communicate between each other.
 * <p>
 * At first, plugins should register their contexts in the plugin root class constructor. Then, within plugin
 * initialization, it can request contexts of other plugins.
 * <p>
 * Whether they get the requested context is given by several factors, like the plugins must be interconnected in
 * the abstract schema, and the contexts must be compatible.
 * <p>
 * Each plugin can implement none, one or more contexts. Implementing the same interface multiple times is allowed.
 * A plugin can also extend this context interface (directly or indirectly).
 * <p>
 * Another requirement is to annotate custom context interfaces with {@link PluginContext} annotation.
 * <p>
 * Contexts should be thread-safe, since there's no guarantee in which threads plugins communicate.
 */
@PluginContext
@ThreadSafe
public interface Context {

}
