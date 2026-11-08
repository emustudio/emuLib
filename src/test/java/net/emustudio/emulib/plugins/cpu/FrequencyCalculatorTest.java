/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.cpu;

import org.junit.Test;
import java.util.concurrent.atomic.AtomicLong;
import static org.junit.Assert.*;

public class FrequencyCalculatorTest {
    @Test
    public void hostSamplingMeasuresAverageAcrossElapsedRunTime() {
        AtomicLong clock = new AtomicLong();
        FrequencyCalculator calculator = new FrequencyCalculator(clock::get);
        calculator.start();
        calculator.passedCycles(2_000_000);

        clock.set(1_000_000_000L);
        assertEquals(2000f, calculator.sample(), 0.001f);

        clock.set(2_000_000_000L);
        assertEquals(1000f, calculator.sample(), 0.001f);
    }

    @Test
    public void stopPublishesPartialBatchAndFreezesMeasurementTime() {
        AtomicLong clock = new AtomicLong();
        FrequencyCalculator calculator = new FrequencyCalculator(clock::get);
        calculator.start();
        calculator.passedCycles(200);
        clock.set(1_000_000_000L);

        calculator.stop();
        calculator.passedCycles(999999);
        clock.set(10_000_000_000L);
        assertEquals(0.2f, calculator.sample(), 0.001f);
    }

    @Test
    public void restartDoesNotCarryEarlierRunCycles() {
        AtomicLong clock = new AtomicLong();
        FrequencyCalculator calculator = new FrequencyCalculator(clock::get);
        calculator.start();
        calculator.passedCycles(2_000_000);
        clock.set(1_000_000_000L);
        calculator.stop();

        calculator.start();
        calculator.passedCycles(1_000_000);
        clock.set(2_000_000_000L);
        assertEquals(1000f, calculator.sample(), 0.001f);
    }

    @Test
    public void emptyMeasurementReturnsUnavailableFrequency() {
        AtomicLong clock = new AtomicLong();
        FrequencyCalculator calculator = new FrequencyCalculator(clock::get);
        calculator.start();
        clock.set(1_000_000_000L);

        assertTrue(Float.isNaN(calculator.sample()));
    }
}
