/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.application;

import net.emustudio.emulib.plugin.cpu.CPU;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/** Host-owned execution snapshots, notifications, and serialized edits for application and plugin views. */
public interface ExecutionState {
    Snapshot snapshot();

    /** Notifications may run outside Swing. Close the subscription when its view is disposed. */
    Subscription subscribe(Consumer<Snapshot> listener);

    /** Serializes a live change between semantic steps, or on the idle CPU worker. */
    CompletableFuture<Void> update(Runnable change);

    @FunctionalInterface
    interface Subscription extends AutoCloseable {
        @Override void close();
    }

    /** Immutable values from one host publication. */
    final class Snapshot {
        private final CPU.RunState runState;
        private final boolean busy, timedRunning;
        private final int currentInstruction;
        private final float frequencyKhz;

        public Snapshot(CPU.RunState runState, boolean busy, boolean timedRunning,
                        int currentInstruction, float frequencyKhz) {
            this.runState = Objects.requireNonNull(runState);
            this.busy = busy;
            this.timedRunning = timedRunning;
            this.currentInstruction = currentInstruction;
            this.frequencyKhz = frequencyKhz;
        }

        public CPU.RunState runState() { return runState; }
        public boolean isBusy() { return busy; }
        public boolean isTimedRunning() { return timedRunning; }
        public int currentInstruction() { return currentInstruction; }
        /** NaN means this CPU does not provide a measurement yet. */
        public float frequencyKhz() { return frequencyKhz; }

        @Override public boolean equals(Object other) {
            if (!(other instanceof Snapshot)) return false;
            Snapshot value = (Snapshot) other;
            return runState == value.runState && busy == value.busy && timedRunning == value.timedRunning
                    && currentInstruction == value.currentInstruction
                    && Float.compare(frequencyKhz, value.frequencyKhz) == 0;
        }

        @Override public int hashCode() {
            return Objects.hash(runState, busy, timedRunning, currentInstruction, frequencyKhz);
        }
    }
}
