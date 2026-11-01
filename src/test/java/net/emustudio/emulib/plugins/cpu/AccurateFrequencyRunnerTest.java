/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.cpu;

import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AccurateFrequencyRunnerTest {

    @Test(timeout = 10000)
    public void testRun() {
        AccurateFrequencyRunner runner = new AccurateFrequencyRunner();
        assertPacedRun(runner, instruction -> runner.run(() -> 100.0, instruction));
    }

    @Test(timeout = 10000)
    public void testRunNoSleep() {
        AccurateFrequencyRunner runner = new AccurateFrequencyRunner();
        assertPacedRun(runner, instruction -> runner.runNoSleep(() -> 100.0, instruction));
    }

    private void assertPacedRun(AccurateFrequencyRunner runner,
                                Function<Supplier<CPU.RunState>, CPU.RunState> run) {
        // 10,000 cycles at 100 kHz require approximately 100 ms.
        int targetCycles = 10000;
        AtomicInteger executedCycles = new AtomicInteger();

        long startTime = System.nanoTime();
        CPU.RunState state = run.apply(() -> {
            runner.addExecutedCycles(1);
            if (executedCycles.incrementAndGet() == targetCycles) {
                return CPU.RunState.STATE_STOPPED_NORMAL;
            }
            return CPU.RunState.STATE_RUNNING;
        });
        long elapsedNanos = System.nanoTime() - startTime;

        assertEquals(CPU.RunState.STATE_STOPPED_NORMAL, state);
        assertEquals(targetCycles, executedCycles.get());
        // Allow slot boundaries and rounding, but reject an unthrottled run.
        // Host scheduling can make execution slower; it cannot guarantee an exact frequency.
        assertTrue("Runner completed too quickly: " + elapsedNanos + " ns",
                elapsedNanos >= TimeUnit.MILLISECONDS.toNanos(90));
    }
}
