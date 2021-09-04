package net.emustudio.emulib.runtime.helpers;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public class SleepUtilsTest {

    @Test
    public void testSleeping() {
        long sleepTime = TimeUnit.MILLISECONDS.toNanos(3);

        SleepUtils.Sleep sleep = SleepUtils.sleep;

        long start = System.nanoTime();
        sleep.sleep(sleepTime);
        long length = System.nanoTime() - start;

        System.out.printf("Should sleep for %d nanos; slept for %d nanos (max allowed: %d nanos)\n", sleepTime, length, (sleepTime + SleepUtils.SLEEP_PRECISION));
        assertTrue(length >= sleepTime);
        assertTrue(length <= (sleepTime + SleepUtils.SLEEP_PRECISION));

        start = System.nanoTime();
        SleepUtils.sleep.sleep(sleepTime);
        length = System.nanoTime() - start;
        System.out.printf("Should sleep for %d nanos; slept for %d nanos (max allowed: %d nanos)\n", sleepTime, length, (sleepTime + SleepUtils.SLEEP_PRECISION));
    }
}