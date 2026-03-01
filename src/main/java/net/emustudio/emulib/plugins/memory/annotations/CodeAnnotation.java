/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.memory.annotations;

/**
 * Memory annotation which represents code.
 */
public class CodeAnnotation extends Annotation {

    /**
     * Constructs a new CodeAnnotation.
     *
     * @param pluginId owner plugin ID
     */
    public CodeAnnotation(long pluginId) {
        super(pluginId);
    }
}
