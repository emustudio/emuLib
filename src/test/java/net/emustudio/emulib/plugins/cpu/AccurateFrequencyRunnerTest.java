/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.cpu;

import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertTrue;

public class AccurateFrequencyRunnerTest {

    @Test
    public void testRun() {
        AccurateFrequencyRunner runner = new AccurateFrequencyRunner();

        FrequencyCalculator calculator = new FrequencyCalculator();
        double frequencyKHz = 100;

        AtomicInteger runFrequency = new AtomicInteger();

        calculator.addListener(f -> runFrequency.set(Math.round(f)));

        long startTime = System.nanoTime();
        calculator.start();
        runner.run(() -> frequencyKHz, () -> {
            runner.addExecutedCycles(1);
            calculator.passedCycles(1);
            long delta = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

            if (delta > 2000) {
                return CPU.RunState.STATE_STOPPED_NORMAL;
            }
            return CPU.RunState.STATE_RUNNING;
        });

        calculator.stop();
        calculator.close();

        assertTrue(frequencyKHz == runFrequency.get() || (frequencyKHz - 1) == runFrequency.get() || (frequencyKHz + 1) == runFrequency.get());
    }

    @Test
    public void testRunNoSleep() {
        AccurateFrequencyRunner runner = new AccurateFrequencyRunner();

        FrequencyCalculator calculator = new FrequencyCalculator();
        double frequencyKHz = 100;

        AtomicInteger runFrequency = new AtomicInteger();

        calculator.addListener(f -> runFrequency.set(Math.round(f)));

        long startTime = System.nanoTime();
        calculator.start();
        runner.runNoSleep(() -> frequencyKHz, () -> {
            runner.addExecutedCycles(1);
            calculator.passedCycles(1);
            long delta = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

            if (delta > 2000) {
                return CPU.RunState.STATE_STOPPED_NORMAL;
            }
            return CPU.RunState.STATE_RUNNING;
        });

        calculator.stop();
        calculator.close();

        assertTrue(frequencyKHz == runFrequency.get() || (frequencyKHz - 1) == runFrequency.get());
    }
}
