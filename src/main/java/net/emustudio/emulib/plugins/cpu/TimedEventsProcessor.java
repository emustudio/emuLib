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

import java.util.Map;
import java.util.Queue;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;
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

    // used on CPU thread only, don't need to be synchronized
    private int clock;

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
        this.cycleMaximum.getAndUpdate(i -> Math.max(i, cycles));

        // I assume most commonly there won't be a cycle clash, but we should support it
        Queue<Runnable> cyclesEvent = new ConcurrentLinkedQueue<>();
        cyclesEvent.add(event);

        Queue<Runnable> prevCyclesEvent = eventQueue.putIfAbsent(cycles, cyclesEvent);
        if (prevCyclesEvent != null) {
            prevCyclesEvent.add(event);
        }
    }

    /**
     * Remove scheduled event from this processor.
     * <p>
     * This function is thread-safe.
     *
     * @param cycles   the number of cycles
     * @param function the scheduled event
     */
    public void remove(int cycles, Runnable function) {
        Queue<Runnable> cyclesEvent = eventQueue.get(cycles);
        cyclesEvent.remove(function);
    }

    /**
     * Schedule an event to be run after given cycles once.
     * <p>
     * This function is thread-safe.
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
        // 1 1 1 1 1 1 1 1
        // 0 2 0 2 0 2 0 2
        // 0 0 3 0 0 3 0 0
        // 0 0 0 4 0 0 0 4
        // ...

        // trigger if (clock + cycle) % key == 0
        Map<Integer, Queue<Runnable>> subMap = eventQueue.subMap(0, clock + cycles + 1);
        for (int i = 1; i <= cycles; i++) {
            int nextCycles = clock + i;
            for (Map.Entry<Integer, Queue<Runnable>> entry : subMap.entrySet()) {
                int atScheduledCycles = entry.getKey();
                if (nextCycles % atScheduledCycles == 0) {
                    entry.getValue().forEach(Runnable::run);
                }
            }
        }

        clock += cycles;

        int currentCycleMaximum = cycleMaximum.get();
        if (clock > currentCycleMaximum) {
            clock = (clock % (currentCycleMaximum + 1));
        }
    }
}
