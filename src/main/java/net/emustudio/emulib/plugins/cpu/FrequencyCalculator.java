/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.cpu;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.function.LongSupplier;

/**
 * Measures average CPU frequency in kHz without owning a thread. The CPU worker calls start,
 * passedCycles, and stop; the host scheduler calls sample. Cycle publication is batched to keep
 * atomic updates out of the instruction loop. Lifecycle/sample locking never covers callbacks.
 */
public final class FrequencyCalculator implements CPUContext.PassedCyclesListener {
    private static final long PUBLICATION_CYCLES = 4096;
    private final LongSupplier nanoTime;
    private final Set<Consumer<Float>> listeners = new CopyOnWriteArraySet<>();
    private long cycles;
    private long nextPublication;
    private volatile long publishedCycles;
    private long startedAt;
    private long stoppedAt;
    private boolean running;

    public FrequencyCalculator() {
        this(System::nanoTime);
    }

    FrequencyCalculator(LongSupplier nanoTime) {
        this.nanoTime = Objects.requireNonNull(nanoTime);
    }

    /** Starts a measurement on the CPU worker. */
    public synchronized void start() {
        cycles = 0;
        publishedCycles = 0;
        nextPublication = PUBLICATION_CYCLES;
        startedAt = nanoTime.getAsLong();
        stoppedAt = startedAt;
        running = true;
    }

    /** Called only by the CPU worker, including start/stop ordering. */
    @Override
    public void passedCycles(long cyclesDelta) {
        if (!running) return;
        cycles += cyclesDelta;
        if (cycles >= nextPublication) {
            publishedCycles = cycles;
            nextPublication = cycles + PUBLICATION_CYCLES;
        }
    }

    /** Publishes the final partial batch after actual execution exit. */
    public synchronized void stop() {
        if (!running) return;
        publishedCycles = cycles;
        stoppedAt = nanoTime.getAsLong();
        running = false;
    }

    /** Called by the host's existing scheduler; never schedules work itself. */
    public void sample() {
        final float frequency;
        synchronized (this) {
            long elapsed = (running ? nanoTime.getAsLong() : stoppedAt) - startedAt;
            long count = publishedCycles;
            if (count == 0 || elapsed <= 0) return;
            frequency = (float) (count / (elapsed / 1_000_000.0));
        }
        listeners.forEach(listener -> listener.accept(frequency));
    }

    public void addListener(Consumer<Float> listener) {
        listeners.add(Objects.requireNonNull(listener));
    }

    public void removeListener(Consumer<Float> listener) {
        listeners.remove(listener);
    }
}
