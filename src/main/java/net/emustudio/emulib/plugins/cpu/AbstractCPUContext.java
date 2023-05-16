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
