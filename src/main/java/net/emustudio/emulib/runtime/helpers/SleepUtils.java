/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Precise sleeping.
 */
public final class SleepUtils {
    private final static Logger LOGGER = LoggerFactory.getLogger(SleepUtils.class);

    /**
     * Average duration of {@code Thread.sleep(1)} in nanoseconds (measured when the class is loaded).
     */
    public final static long SLEEP_PRECISION;

    static {
        int count = 600;
        long time = 0;
        for (int i = 0; i < count; i++) {
            long start = System.nanoTime();
            sleepMillisecond();
            time += (System.nanoTime() - start);
        }
        SLEEP_PRECISION = time / count;
        LOGGER.debug("Sleep precision: {} ns", SLEEP_PRECISION);
    }

    private SleepUtils() {
    }

    /**
     * Accurately sleeps on all platforms (Linux, Win, Mac); more precise than {@code LockSupport.parkNanos}.
     * <p>
     * Sleeps while the remaining time exceeds {@link #SLEEP_PRECISION}, then spin-waits. Returns early if the
     * thread is interrupted (the interrupt flag is kept). See
     * <a href="https://andy-malakov.blogspot.com/2010/06/alternative-to-threadsleep.html">alternative to Thread.sleep</a>.
     *
     * @param nanoDuration nanoseconds
     */
    public static void preciseSleepNanos(long nanoDuration) {
        final long end = System.nanoTime() + nanoDuration;
        long timeLeft = nanoDuration;
        do {
            if (timeLeft > SLEEP_PRECISION) {
                sleepMillisecond();
            } else if (timeLeft > SLEEP_PRECISION / 2) {
                Thread.onSpinWait();
            }
            timeLeft = end - System.nanoTime();
        } while (!Thread.currentThread().isInterrupted() && timeLeft > 0);
    }

    private static void sleepMillisecond() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
