/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.cpu;

import org.junit.Test;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import static org.junit.Assert.*;

public class FrequencyCalculatorTest {
    @Test
    public void hostSamplingMeasuresAverageAcrossElapsedRunTime() {
        AtomicLong clock = new AtomicLong();
        FrequencyCalculator calculator = new FrequencyCalculator(clock::get);
        AtomicReference<Float> frequency = new AtomicReference<>();
        calculator.addListener(frequency::set);
        calculator.start();
        calculator.passedCycles(2_000_000);

        clock.set(1_000_000_000L);
        calculator.sample();
        assertEquals(2000f, frequency.get(), 0.001f);

        clock.set(2_000_000_000L);
        calculator.sample();
        assertEquals(1000f, frequency.get(), 0.001f);
    }

    @Test
    public void stopPublishesPartialBatchAndFreezesMeasurementTime() {
        AtomicLong clock = new AtomicLong();
        FrequencyCalculator calculator = new FrequencyCalculator(clock::get);
        AtomicReference<Float> frequency = new AtomicReference<>();
        calculator.addListener(frequency::set);
        calculator.start();
        calculator.passedCycles(200);
        clock.set(1_000_000_000L);

        calculator.stop();
        calculator.passedCycles(999999);
        clock.set(10_000_000_000L);
        calculator.sample();

        assertEquals(0.2f, frequency.get(), 0.001f);
    }

    @Test
    public void restartDoesNotCarryEarlierRunCycles() {
        AtomicLong clock = new AtomicLong();
        FrequencyCalculator calculator = new FrequencyCalculator(clock::get);
        AtomicReference<Float> frequency = new AtomicReference<>();
        calculator.addListener(frequency::set);
        calculator.start();
        calculator.passedCycles(2_000_000);
        clock.set(1_000_000_000L);
        calculator.stop();

        calculator.start();
        calculator.passedCycles(1_000_000);
        clock.set(2_000_000_000L);
        calculator.sample();

        assertEquals(1000f, frequency.get(), 0.001f);
    }

    @Test
    public void removedListenerAndZeroCyclesProduceNoNotification() {
        AtomicLong clock = new AtomicLong();
        FrequencyCalculator calculator = new FrequencyCalculator(clock::get);
        AtomicReference<Float> frequency = new AtomicReference<>();
        Consumer<Float> listener = frequency::set;
        calculator.addListener(listener);
        calculator.start();
        clock.set(1_000_000_000L);
        calculator.sample();
        assertNull(frequency.get());

        calculator.removeListener(listener);
        calculator.passedCycles(1_000_000);
        calculator.sample();

        assertNull(frequency.get());
    }
}
