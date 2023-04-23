/*
 * This file is part of emuLib.
 *
 * Copyright (C) 2006-2023  Peter Jakubčo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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

