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
