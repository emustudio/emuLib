/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugin.cpu;

import net.jcip.annotations.ThreadSafe;

import java.util.Set;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Abstract CPU context.
 * <p>
 * Implements passed cycles listeners management
 */
@ThreadSafe
public abstract class AbstractCPUContext implements CPUContext {

    private final Set<PassedCyclesListener> passedCyclesListenerSet = new CopyOnWriteArraySet<>();
    private volatile CPU.RunState runState = CPU.RunState.STATE_STOPPED_BREAK;
    private volatile int instructionLocation = -1;
    private volatile float measuredFrequencyKhz = Float.NaN;
    private final Set<CPUListener> cpuListeners = new CopyOnWriteArraySet<>();

    /**
     * Constructs new abstract CPU context.
     */
    protected AbstractCPUContext() {
    }

    @Override
    public CPU.RunState getRunState() {
        return runState;
    }

    @Override
    public int getInstructionLocation() {
        return instructionLocation;
    }

    @Override
    public float getCpuFrequencyKhz() {
        return measuredFrequencyKhz;
    }

    @Override
    public void addCPUListener(CPUListener listener) {
        cpuListeners.add(Objects.requireNonNull(listener));
    }

    @Override
    public void removeCPUListener(CPUListener listener) {
        cpuListeners.remove(listener);
    }

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

    /**
     * CPU implementations publish their own state; this is not part of the context interface.
     */
    protected final void fireStateChanged(CPU.RunState next, int instructionLocation, float measuredFrequencyKhz) {
        Objects.requireNonNull(next);
        CPU.RunState previous = runState;
        this.instructionLocation = instructionLocation;
        this.measuredFrequencyKhz = measuredFrequencyKhz;
        runState = next;
        for (CPUListener listener : cpuListeners) {
            if (previous != next) notifyListener(() -> listener.runStateChanged(next));
            notifyListener(listener::internalStateChanged);
        }
    }

    private void notifyListener(Runnable notification) {
        try {
            notification.run();
        } catch (RuntimeException failure) {
            java.util.logging.Logger.getLogger(AbstractCPUContext.class.getName())
                    .log(java.util.logging.Level.WARNING, "CPU listener failed", failure);
        }
    }
}
