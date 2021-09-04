package net.emustudio.emulib.runtime.helpers;


public class SleepUtils {
    public final static long SLEEP_PRECISION;
    public final static long SPIN_YIELD_PRECISION;

    public final static Sleep sleep;

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

        // even better than LockSupport::parkNanos
        sleep = SleepUtils::preciseSleepNanos;
    }


    /**
     * Accurately sleeps on all platforms (Linux, Win, Mac).
     * Sleep precision: SLEEP_PRECISION
     *
     * See:
     *  - https://stackoverflow.com/questions/824110/accurate-sleep-for-java-on-windows
     *  - https://andy-malakov.blogspot.com/2010/06/alternative-to-threadsleep.html
     * @param nanoDuration nanoseconds
     */
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
                Thread.yield();
            }
            timeLeft = end - System.nanoTime();
        } while (timeLeft > 0);
    }

    @FunctionalInterface
    interface Sleep {

        /**
         * Accurate sleep. Works on all platforms.
         * @param nanoDuration nanoseconds
         */
        void sleep(long nanoDuration);
    }
}
