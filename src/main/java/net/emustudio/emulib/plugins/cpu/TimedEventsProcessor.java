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

import net.emustudio.emulib.runtime.helpers.ReadWriteLockSupport;
import net.jcip.annotations.ThreadSafe;

import java.util.Collection;
import java.util.Queue;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Timed events processing is a soft real-time system based on a logical system clock,
 * interpreted as number of passed CPU cycles.
 * <p>
 * Events (Runnable objects) are scheduled to be run every given cycles. Scheduling events is a
 * thread-safe operation, so any device can register an event handler (= schedule an event) from
 * any thread.
 * <p>
 * CPU is responsible for advancing the clock after each executed instruction
 * (see {@link TimedEventsProcessor#advanceClock(int) advanceClock} method),
 * which is supposed to be run on the CPU emulator thread. The events which passed their deadline
 * are then triggered on the same thread (CPU thread).
 * <p>
 * The event "deadline" is a number of cycles passed from current executed number of cycles. So events can
 * be scheduled to run e.g. every 5 cycles.
 * <p>
 * If the CPU supports timed events processing, it should return an instance of this class in the
 * {@link CPUContext#getTimedEventsProcessor() getTimedEventsProcessor} method.
 */
@ThreadSafe
public class TimedEventsProcessor {
    private final AtomicInteger cycleMaximum = new AtomicInteger(0);
    private final SortedMap<Integer, Queue<Runnable>> eventQueue = new ConcurrentSkipListMap<>();
    private final ReadWriteLockSupport lock = new ReadWriteLockSupport();

    // scheduled cycles
    private final SortedSet<Integer> usedCycleRoots = new ConcurrentSkipListSet<>();

    // used on CPU thread only, don't need to be synchronized
    private int clock;
    private int lastProcessedCycles = 0;

    /**
     * Schedule a repeated event to be run every given cycles.
     * <p>
     * This function is thread-safe.
     *
     * @param cycles the number of cycles (must be &gt; 0)
     * @param event  event to be triggered every given cycles
     */
    public void schedule(int cycles, Runnable event) {
        if (cycles <= 0) {
            throw new IllegalArgumentException("Allowed cycles schedule for an event must be > 0");
        }
        lock.lockWrite(() -> {
            int oldMaximum = cycleMaximum.getAndUpdate(i -> Math.max(i, cycles));
            int newMaximum = Math.max(oldMaximum, cycles);

            // 1 1 1 1 1 1 1 1
            // 0 2 0 2 0 2 0 2
            // 0 0 3 0 0 3 0 0
            // 0 0 0 4 0 0 0 4
            // ...

            // copy over already scheduled cycles
            for (int i = oldMaximum + 1; i <= newMaximum; i++) {
                for (int cycleRoot : usedCycleRoots) {
                    if (i % cycleRoot == 0) {
                        // should be absent on normal circumstances - non-absent only if another thread is faster
                        eventQueue.computeIfAbsent(i, k -> new ConcurrentLinkedQueue<>(eventQueue.get(cycleRoot)));
                    }
                }
            }

            // schedule this event
            usedCycleRoots.add(cycles);
            Queue<Runnable> prevCyclesEvent = eventQueue.computeIfAbsent(cycles, c -> new ConcurrentLinkedQueue<>());
            prevCyclesEvent.add(event);
        });
    }

    /**
     * Remove scheduled event from this processor.
     * <p>
     * This function is thread-safe.
     * <p>
     * Given event function must be the same instance as used on scheduling. If the same function instance was used for
     * different cycles, which are divisible with given cycles (higher % lower == 0), the function will be removed
     * for cycles which were scheduled first (always the lower ones). In effect the behavior should be as expected
     * though.
     * <p>
     * Also, this function does not work when the event was scheduled using
     * {@link #scheduleOnce(int, Runnable) scheduleOnce} function.
     *
     * @param cycles the number of cycles
     * @param event  the scheduled event
     */
    public void remove(int cycles, Runnable event) {
        lock.lockWrite(() -> {
            if (usedCycleRoots.remove(cycles)) {
                int maximum = cycleMaximum.get();
                for (int i = cycles; i <= maximum; i++) {
                    if (i % cycles == 0) {
                        Queue<Runnable> iEvent = eventQueue.get(i);
                        iEvent.remove(event);
                    }
                }
            }
        });
    }

    /**
     * Remove all scheduled events from this processor.
     * <p>
     * This function is thread-safe.
     *
     * @param cycles the number of cycles
     */
    public void removeAll(int cycles) {
        lock.lockWrite(() -> {
            if (usedCycleRoots.remove(cycles)) {
                int maximum = cycleMaximum.get();
                for (int i = cycles; i <= maximum; i++) {
                    if (i % cycles == 0) {
                        eventQueue.remove(i);
                    }
                }
            }
        });
    }

    /**
     * Schedule an event to be run after given cycles once.
     * <p>
     * This function is thread-safe.
     * <p>
     * Given event cannot be removed by calling {@link #remove(int, Runnable) remove()} function.
     *
     * @param cycles the number of cycles (must be &gt; 0)
     * @param event  event to be triggered every given cycles
     */
    public void scheduleOnce(int cycles, Runnable event) {
        AtomicReference<Runnable> selfReference = new AtomicReference<>();
        Runnable proxy = () -> {
            event.run();
            remove(cycles, selfReference.get());
        };
        selfReference.set(proxy);

        schedule(cycles, proxy);
    }

    /**
     * Advances system clock (number of passed CPU cycles) and triggers all events
     * which passed their deadline.
     * <p>
     * This function should be called from a CPU emulation thread; thus it is not thread-safe.
     *
     * @param cycles passed cycles in the system
     */
    public void advanceClock(int cycles) {
        int currentCycleMaximum = cycleMaximum.get();
        clock += cycles;

        Collection<Queue<Runnable>> eventsToTrigger = eventQueue
                .subMap(lastProcessedCycles, clock + 1)
                .values();

        if (clock < currentCycleMaximum) {
            eventsToTrigger.forEach(e -> e.forEach(Runnable::run));
        } else if (currentCycleMaximum > 0) {
            for (int i = 0; i < clock / currentCycleMaximum; i++) {
                eventsToTrigger.forEach(e -> e.forEach(Runnable::run));
            }
        }

        lastProcessedCycles = clock + 1;
        if (clock > currentCycleMaximum) {
            clock = (clock % (currentCycleMaximum + 1));
            lastProcessedCycles = 0;
        }
    }
}
