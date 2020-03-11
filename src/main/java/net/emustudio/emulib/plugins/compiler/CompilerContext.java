// SPDX-License-Identifier: GPL-3.0-or-later
package net.emustudio.emulib.plugins.compiler;

import net.emustudio.emulib.plugins.annotations.PluginContext;
import net.emustudio.emulib.plugins.Context;

/**
 * Compiler context can be used to provide additional meta-information for runtime, which can be used by other plugins.
 *
 * Plugins which need the specific compiler contexts, should declare a dependency on the compiler plugin.
 */
@PluginContext
public interface CompilerContext extends Context {

}

