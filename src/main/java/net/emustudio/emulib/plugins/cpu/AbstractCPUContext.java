/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.cpu;

import net.jcip.annotations.ThreadSafe;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Abstract CPU context.
 * <p>
 * Implements passed cycles listeners management
 */
@ThreadSafe
public abstract class AbstractCPUContext implements CPUContext {

    /**
     * Constructs a new AbstractCPUContext.
     */
    protected AbstractCPUContext() {
    }

    private final Set<PassedCyclesListener> passedCyclesListenerSet = new CopyOnWriteArraySet<>();

    @Override
    public void addPassedCyclesListener(PassedCyclesListener passedCyclesListener) {
        passedCyclesListenerSet.add(passedCyclesListener);
    }

    @Override
    public void removePassedCyclesListener(PassedCyclesListener passedCyclesListener) {
        passedCyclesListenerSet.remove(passedCyclesListener);
    }

    /**
     * Notifies passed cycle listeners that given CPU cycles has passed since last call (a cycles delta).
     * <p>
     * This method should be called by CPU.
     *
     * @param cycles passed cycles delta
     */
    public void passedCycles(long cycles) {
        passedCyclesListenerSet.forEach(c -> c.passedCycles(cycles));
    }
}
