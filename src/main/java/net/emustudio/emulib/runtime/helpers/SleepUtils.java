/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sleeping and time measurement utilities.
 */
public class SleepUtils {
    private final static Logger LOGGER = LoggerFactory.getLogger(SleepUtils.class);

    /**
     * Constructs a new SleepUtils instance.
     */
    private SleepUtils() {
    }

    /**
     * Sleep precision in nanoseconds (determined automatically).
     */
    public final static long SLEEP_PRECISION;
    /**
     * Spin yield precision in nanoseconds (half of sleep precision).
     */
    public final static long SPIN_YIELD_PRECISION;

    static {
        // determine sleep precision
        int count = 600;
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

        LOGGER.debug("Sleep precision: {} ns", SLEEP_PRECISION);
        LOGGER.debug("Spin yield precision: {} ns", SPIN_YIELD_PRECISION);
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
