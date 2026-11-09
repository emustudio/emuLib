/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugin.cpu;

import java.util.Objects;

/**
 * Immutable CPU state and execution details.
 */
public final class CPUSnapshot {
    private final CPU.RunState runState;
    private final boolean busy, timedRunning;
    private final int currentInstruction;
    private final float frequencyKhz;

    public CPUSnapshot(CPU.RunState runState, boolean busy, boolean timedRunning,
                       int currentInstruction, float frequencyKhz) {
        this.runState = Objects.requireNonNull(runState);
        this.busy = busy;
        this.timedRunning = timedRunning;
        this.currentInstruction = currentInstruction;
        this.frequencyKhz = frequencyKhz;
    }

    public CPU.RunState runState() {
        return runState;
    }

    public boolean isBusy() {
        return busy;
    }

    public boolean isTimedRunning() {
        return timedRunning;
    }

    public int currentInstruction() {
        return currentInstruction;
    }

    /**
     * NaN means this CPU does not provide a measurement yet.
     */
    public float frequencyKhz() {
        return frequencyKhz;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof CPUSnapshot)) return false;
        CPUSnapshot value = (CPUSnapshot) other;
        return runState == value.runState && busy == value.busy && timedRunning == value.timedRunning
                && currentInstruction == value.currentInstruction
                && Float.compare(frequencyKhz, value.frequencyKhz) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(runState, busy, timedRunning, currentInstruction, frequencyKhz);
    }
}
