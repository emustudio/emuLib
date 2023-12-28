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
package net.emustudio.emulib.runtime.helpers;

/**
 * Sleeping and time measurement utilities.
 */
public class SleepUtils {
    public final static long SLEEP_PRECISION;
    public final static long SPIN_YIELD_PRECISION;

    static {
        // determine sleep precision
        int count = 100;
        long time = 0;
        for (int i = 0; i < count; i++) {
            long start = System.nanoTime();
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            time += (System.nanoTime() - start);
        }
        SLEEP_PRECISION = time / count;
        SPIN_YIELD_PRECISION = SLEEP_PRECISION / 2;
    }


    /**
     * Accurately sleeps on all platforms (Linux, Win, Mac).
     * Sleep precision: SLEEP_PRECISION (determined automatically)
     * <p>
     * It is more precise than LockSupport::sleepNanos
     * <p>
     * See:
     * - <a href="https://stackoverflow.com/questions/824110/accurate-sleep-for-java-on-windows">accurate sleep for java</a>
     * - <a href="https://andy-malakov.blogspot.com/2010/06/alternative-to-threadsleep.html">alternative to Thread.sleep</a>
     *
     * @param nanoDuration nanoseconds
     */
    @SuppressWarnings("BusyWait")
    public static void preciseSleepNanos(long nanoDuration) {
        final long end = System.nanoTime() + nanoDuration;
        long timeLeft = nanoDuration;

        do {
            if (timeLeft > SLEEP_PRECISION) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else if (timeLeft > SPIN_YIELD_PRECISION) {
                Thread.onSpinWait();
            }
            timeLeft = end - System.nanoTime();
        } while (!Thread.currentThread().isInterrupted() && timeLeft > 0);
    }
}
