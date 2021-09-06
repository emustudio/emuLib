package net.emustudio.emulib.runtime.helpers;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public class SleepUtilsTest {

    @Test
    public void testSleeping() {
        long sleepTime = TimeUnit.MILLISECONDS.toNanos(3);
        long maxAllowedTime = sleepTime + SleepUtils.SLEEP_PRECISION;

        long start = System.nanoTime();
        SleepUtils.preciseSleepNanos(sleepTime);
        long length = System.nanoTime() - start;

        System.out.printf("Should sleep for %d nanos; slept for %d (max allowed: %d)\n", sleepTime, length, maxAllowedTime);
        assertTrue(length >= sleepTime);
        assertTrue(length <= maxAllowedTime);
    }
}
