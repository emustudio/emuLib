/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.annotations;

/**
 * Indicates type of the plugin.
 */
@SuppressWarnings("unused")
public enum PLUGIN_TYPE {
    /**
     * Compiler plugin.
     */
    COMPILER,
    /**
     * CPU plugin.
     */
    CPU,
    /**
     * Memory plugin.
     */
    MEMORY,
    /**
     * Device plugin.
     */
    DEVICE
}
