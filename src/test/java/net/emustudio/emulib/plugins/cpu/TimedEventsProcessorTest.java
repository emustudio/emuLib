/*
 * This file is part of emuLib.
 *
 * Copyright (C) 2006-2022  Peter Jakubčo
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

        tep.advanceClock(5); // should trigger the first event
        tep.advanceClock(44); // should do nothing
        tep.advanceClock(1);  // should trigger the second event

        assertEquals(2, count.get());
    }

    @Test
    public void testScheduleAndAdvanceOverflowWorks() {
        AtomicInteger count = new AtomicInteger();

        tep.schedule(5, count::incrementAndGet);
        tep.advanceClock(30); // should trigger the event once
        assertEquals(1, count.get());
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
}
