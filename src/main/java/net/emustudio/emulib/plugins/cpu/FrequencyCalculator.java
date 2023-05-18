package net.emustudio.emulib.plugins.cpu;

import net.jcip.annotations.ThreadSafe;

import java.io.Closeable;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * CPU real frequency calculator
 * <p>
 * Calculates frequency of CPU cycles in kHZ. Internally it is using a separate thread for cycles sampling,
 * running each second.
 * <p>
 * Over time, CPU executes instructions which take a few cycles. CPU notifies this calculator independently.
 * If we sample how many cycles were executed each second, we can compute cycles per second, which is the frequency in Hz.
 * <p>
 * In order to improve the accuracy, each second the executed cycles sample is added to "global" cycles counter, divided
 * by "global" time period (counted from frequency calculator start to now).
 */
@ThreadSafe
public class FrequencyCalculator implements CPUContext.PassedCyclesListener, Closeable {
    private final AtomicLong cycles = new AtomicLong();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final AtomicReference<Future<Runnable>> frequencyUpdaterFuture = new AtomicReference<>();
    private final Set<Consumer<Float>> frequencyChangedListeners = new CopyOnWriteArraySet<>();

    private class Updater implements Runnable {
        private final long startTime = System.nanoTime();
        private float frequencyKhz;

        public Updater() {
            cycles.set(0);
        }

        @Override
        public void run() {
            boolean frequencyChanged = false;

            synchronized (this) {
                long endTime = System.nanoTime();
                long time = endTime - startTime;
                double executedCycles = cycles.get();

                if (executedCycles > 0) {
                    frequencyKhz = (float) (executedCycles / (time / 1000000.0)); // per millisecond = kHz
                    frequencyChanged = true;
                }
            }

            if (frequencyChanged) {
                notifyListeners();
            }
        }

        private void notifyListeners() {
            float localFrequencyKhz = frequencyKhz;
            for (Consumer<Float> listener : frequencyChangedListeners) {
                listener.accept(localFrequencyKhz);
            }
        }
    }

    @Override
    public void passedCycles(long cyclesDelta) {
        if (frequencyUpdaterFuture.get() != null) {
            this.cycles.addAndGet(cyclesDelta);
        }
    }

    public void addListener(Consumer<Float> listener) {
        frequencyChangedListeners.add(listener);
    }

    public void removeListener(Consumer<Float> listener) {
        frequencyChangedListeners.remove(listener);
    }

    public void stop() {
        Future<Runnable> tmpFuture;
        do {
            tmpFuture = frequencyUpdaterFuture.get();
            if (tmpFuture != null) {
                tmpFuture.cancel(false);
            }
        } while (!frequencyUpdaterFuture.compareAndSet(tmpFuture, null));
    }

    @SuppressWarnings("unchecked")
    public void start() {
        Future<Runnable> tmpFuture;
        Future<Runnable> newFuture = (Future<Runnable>) executor.scheduleAtFixedRate(new Updater(), 0, 1, TimeUnit.SECONDS);

        do {
            tmpFuture = frequencyUpdaterFuture.get();
            if (tmpFuture != null) {
                tmpFuture.cancel(false);
            }
        } while (!frequencyUpdaterFuture.compareAndSet(tmpFuture, newFuture));
    }

    @Override
    public void close() {
        executor.shutdown();
    }
}
