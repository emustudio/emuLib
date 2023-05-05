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

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class TimedEventsProcessorTest {

    private TimedEventsProcessor tep;

    @Before
    public void setUp() {
        this.tep = new TimedEventsProcessor();
    }

    @Test
    public void testScheduleAndAdvanceWorks() {
        AtomicInteger count = new AtomicInteger();

        tep.schedule(5, count::incrementAndGet);
        tep.schedule(50, count::incrementAndGet);
        tep.schedule(100, count::incrementAndGet);

        tep.advanceClock(5); // should trigger the first event 1x
        tep.advanceClock(44); // should trigger the first event 8x
        tep.advanceClock(1);  // should trigger the second event 1x and the first event 1x

        assertEquals(11, count.get());
    }

    @Test
    public void testScheduleAndAdvanceOverflowWorks() {
        AtomicInteger count = new AtomicInteger();

        tep.schedule(5, count::incrementAndGet);
        tep.advanceClock(30); // should trigger the event 30/5 = 6x
        assertEquals(6, count.get());
    }

    @Test
    public void testRemoveScheduleWorks() {
        AtomicInteger count = new AtomicInteger();

        Runnable function = count::incrementAndGet;
        tep.schedule(5, function);
        tep.remove(5, function);
        tep.advanceClock(5);
        assertEquals(0, count.get());
    }

    @Test
    public void testAdvanceWithoutScheduleDoesNotThrow() {
        tep.advanceClock(5);
    }

    @Test
    public void testScheduleOnce() {
        AtomicInteger count = new AtomicInteger();
        tep.scheduleOnce(1, count::incrementAndGet);
        tep.advanceClock(1);
        tep.advanceClock(1);
        tep.advanceClock(1);
        assertEquals(1, count.get());
    }

    @Test
    public void testRemoveCyclesRemovesDerivedOnes() {
        AtomicInteger count = new AtomicInteger();
        Runnable r1 = count::incrementAndGet;

        tep.schedule(1, r1);
        tep.schedule(5, r1);
        tep.remove(1, r1);

        tep.advanceClock(5);
        assertEquals(1, count.get());
    }

    @Test
    public void testRemoveAll() {
        AtomicInteger count = new AtomicInteger();

        tep.schedule(1, count::incrementAndGet);
        tep.schedule(10, count::incrementAndGet);
        tep.removeAll(1);

        tep.advanceClock(10);
        assertEquals(0, count.get());
    }

    @Test
    public void testScheduleMaximumFillUp() {
        AtomicInteger count = new AtomicInteger();
        tep.schedule(10, count::incrementAndGet);  // 10
        tep.schedule(2, count::incrementAndGet);   // 2 4 6 8 10

        tep.advanceClock(10);
        assertEquals(6, count.get());
    }
}
