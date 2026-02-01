/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.cpu;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FrequencyCalculatorTest {

    @Test
    public void testFrequencyNotChanged() {
        AtomicReference<Float> counter = new AtomicReference<>(0.0f);
        try (FrequencyCalculator f = new FrequencyCalculator()) {
            f.addListener(counter::set);
            f.start();
            Thread.sleep(1000);
            f.stop();
        } catch (InterruptedException ignored) {
        }

        assertEquals(0.0, (double) counter.get(), 0.0);
    }

    @Test
    public void testFrequencyChanged() {
        AtomicReference<Float> counter = new AtomicReference<>();

        try (FrequencyCalculator f = new FrequencyCalculator()) {
            f.addListener(counter::set);
            f.start();
            f.passedCycles(2000);
            Thread.sleep(1500);
            f.stop();
        } catch (InterruptedException ignored) {
        }
        assertNotNull(counter.get());
    }


    @Test
    public void testFrequencyChanged2() {
        AtomicReference<Float> counter = new AtomicReference<>(0.0f);
        try (FrequencyCalculator f = new FrequencyCalculator()) {
            f.addListener(counter::set);
            f.start();
            f.passedCycles(2000000); // 1 MHz = 1000 kHz = 1000000 Hz
            Thread.sleep(2500); // should execute the updater two times
            f.stop();
        } catch (InterruptedException ignored) {

        }
        assertEquals(1000, Math.round(counter.get() / 10.0) * 10);
    }

    @Test
    public void testAddRemoveListener() {
        AtomicReference<Float> counter = new AtomicReference<>(0.0f);
        Consumer<Float> fun = counter::set;
        try (FrequencyCalculator f = new FrequencyCalculator()) {
            f.addListener(fun);
            f.removeListener(fun); // should not react to freq change
            f.start();
            f.passedCycles(2000);
            Thread.sleep(1500);
            f.stop();
        } catch (InterruptedException ignored) {
        }
        assertEquals(0.0, (double) counter.get(), 0.0);
    }
}
