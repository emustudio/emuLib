/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.memory.annotations;

import net.jcip.annotations.Immutable;

/**
 * Breakpoint annotation.
 * <p>
 * If CPU encounters this annotation it should break running.
 */
@Immutable
public class BreakpointAnnotation extends Annotation {

    public BreakpointAnnotation(long sourcePluginId) {
        super(sourcePluginId);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
