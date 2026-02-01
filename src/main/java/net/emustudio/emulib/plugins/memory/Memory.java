/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.memory;

import net.emustudio.emulib.plugins.Plugin;
import net.emustudio.emulib.plugins.memory.annotations.MemoryAnnotations;

/**
 * Memory plugin root interface.
 * <p>
 * Memory can define a "memory context", which can enable additional non-standard functionality, which can be used
 * by other plugins.
 *
 * @see MemoryContext
 */
@SuppressWarnings("unused")
public interface Memory extends Plugin {

    @Override
    default boolean isAutomationSupported() {
        return true;
    }

    /**
     * Get memory annotations
     *
     * @return memory annotations
     */
    MemoryAnnotations getAnnotations();

    /**
     * Get memory size
     *
     * @return memory size
     */
    int getSize();
}

