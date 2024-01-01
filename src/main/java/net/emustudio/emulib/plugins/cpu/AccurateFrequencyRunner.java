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

import net.emustudio.emulib.runtime.helpers.SleepUtils;
import net.jcip.annotations.NotThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Accurate Frequency Runner.
 * <p>
 * This class can be used for emulating CPUs which need precise timing. Given a target frequency, it will try to run the
 * instructions in this frequency, regardless of host CPU speed.
 * <p>
 * The algorithm is based on accurate counting of executed CPU cycles per fixed time slot.
 */
@NotThreadSafe
public class AccurateFrequencyRunner {
    private final static Logger LOGGER = LoggerFactory.getLogger(AccurateFrequencyRunner.class);

    private final long slotNanos = SleepUtils.SLEEP_PRECISION;
    private final double slotMicros = slotNanos / 1000.0;

    private final AtomicLong executedCycles = new AtomicLong();

    /**
     * Runs the CPU.
     * <p>
     * The "runInstruction" function must call {@link #addExecutedCycles(long)} to update executed cycles.
     *
     * @param getTargetFrequencyKHz get target frequency in kHz
     * @param runInstruction        runs one instruction
     * @return new run state
     */
    @SuppressWarnings("BusyWait")
    public CPU.RunState run(Supplier<Double> getTargetFrequencyKHz, Supplier<CPU.RunState> runInstruction) {
        final int cyclesPerSlot = Math.max(1, (int) (slotMicros * getTargetFrequencyKHz.get() / 1000.0));

        LOGGER.debug("Running CPU with {} cycles per slot", cyclesPerSlot);

        CPU.RunState currentRunState = CPU.RunState.STATE_RUNNING;
        long delayNanos = slotNanos;

        executedCycles.set(0);

        long emulationStartTime = System.nanoTime();
        while (!Thread.currentThread().isInterrupted() && (currentRunState == CPU.RunState.STATE_RUNNING)) {
            try {
                if (delayNanos > 0) {
                    // We do not require precise sleep here!
                    Thread.sleep(TimeUnit.NANOSECONDS.toMillis(delayNanos));
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            long computationStartTime = System.nanoTime();

            // We take into consideration real sleep time
            long targetCycles = (computationStartTime - emulationStartTime) / slotNanos * cyclesPerSlot;

            // overflow?
            if (targetCycles < 0 || executedCycles.get() < 0) {
                targetCycles = Math.abs(targetCycles % Long.MAX_VALUE);
                executedCycles.updateAndGet(c -> Math.abs(c % Long.MAX_VALUE));
            }

            while ((executedCycles.get() < targetCycles) &&
                    !Thread.currentThread().isInterrupted() &&
                    (currentRunState == CPU.RunState.STATE_RUNNING)) {
                currentRunState = runInstruction.get();
            }

            long computationTime = System.nanoTime() - computationStartTime;
            delayNanos = slotNanos - computationTime;
        }
        return currentRunState;
    }

    /**
     * Adds executed cycles to the counter.
     * <p>
     * Should be called from "runInstruction" function when calling {@link #run(Supplier, Supplier)} method.
     *
     * @param cycles number of cycles
     */
    public void addExecutedCycles(long cycles) {
        executedCycles.addAndGet(cycles);
    }
}