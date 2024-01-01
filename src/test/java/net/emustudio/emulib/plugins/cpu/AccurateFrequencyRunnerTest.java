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

        assertTrue(frequencyKHz == runFrequency.get() || (frequencyKHz - 1) == runFrequency.get());
    }

    @Test
    public void testOo() {
        long l = Long.MAX_VALUE;

        System.out.println(l);

        System.out.println(l + 1);
        System.out.println(Math.abs((l + 1) % Long.MAX_VALUE));
    }
}
