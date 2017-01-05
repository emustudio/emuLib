/*
 * KISS, YAGNI, DRY
 *
 * (c) Copyright 2006-2016, Peter Jakubčo
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package emulib.plugins.cpu;

import emulib.annotations.PluginType;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import net.jcip.annotations.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements some fundamental functionality that can be used by your own plug-ins. Such as:
 *
 * - support of breakpoints
 * - thread safe controlling of run states
 * - managing CPU state listeners
 *
 */
@ThreadSafe
@SuppressWarnings("unused")
public abstract class AbstractCPU implements CPU, Callable<CPU.RunState> {
    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractCPU.class);
    private final static Runnable EMPTY_TASK = () -> {};

    private final AtomicBoolean isDestroyed = new AtomicBoolean();
    private final ExecutorService eventReceiver = Executors.newSingleThreadExecutor();

    private final ExecutorService cpuExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService cpuStoppedWatcher = Executors.newSingleThreadExecutor();

    private final long pluginID;
    private final Set<CPUListener> stateObservers = new CopyOnWriteArraySet<>();
    private final Set<Integer> breakpoints = new ConcurrentSkipListSet<>();

    // ** CONTRACT: set only in "eventReceiver" or "cpuWatchTask" in a non-concurrent way **
    private volatile RunState runState = RunState.STATE_STOPPED_NORMAL;
    // set only in "execute" event
    private volatile CPUWatchTask cpuWatchTask;
    // ** END OF CONTRACT **

    private class CPUWatchTask implements Runnable {
        private final Future<RunState> cpuFuture;

        private CPUWatchTask(Future<RunState> cpuFuture) {
            this.cpuFuture = Objects.requireNonNull(cpuFuture);
        }

        @Override
        public void run() {
            try {
                runState = cpuFuture.get();
            } catch (ExecutionException e) {
                if (e.getCause() instanceof IndexOutOfBoundsException) {
                    runState = RunState.STATE_STOPPED_ADDR_FALLOUT;
                } else {
                    Throwable cause = e.getCause().getCause();
                    if (cause != null && (cause instanceof IndexOutOfBoundsException)) {
                        runState = RunState.STATE_STOPPED_ADDR_FALLOUT;
                    } else {
                        runState = RunState.STATE_STOPPED_BAD_INSTR;
                    }
                }
                LOGGER.error("Unexpected error during emulation", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                notifyStateChanged();
            }
        }

        void requestStop() {
            cpuFuture.cancel(true);
        }
    }

    /**
     * Creates new instance of CPU.
     *
     * @param pluginID plug-in identification number
     * @throws NullPointerException if pluginID is null
     */
    public AbstractCPU(Long pluginID) {
        this.pluginID = Objects.requireNonNull(pluginID);
    }

    /**
     * Get plug-in ID assigned by emuStudio.
     *
     * @return plug-in ID
     */
    protected long getPluginID() {
        return pluginID;
    }

    @Override
    public final String getTitle() {
        return getClass().getAnnotation(PluginType.class).title();
    }

    /**
     * Does nothing.
     */
    @Override
    public void showSettings() {

    }

    /**
     * This class does not support showing settings
     *
     * @return false
     */
    @Override
    public boolean isShowSettingsSupported() {
        return false;
    }

    /**
     * This class support breakpoints.
     *
     * @return true
     */
    @Override
    public boolean isBreakpointSupported() {
        return true;
    }

    @Override
    public void setBreakpoint(int memLocation) {
        breakpoints.add(memLocation);
    }

    @Override
    public void unsetBreakpoint(int memLocation) {
        breakpoints.remove(memLocation);
    }

    @Override
    public boolean isBreakpointSet(int memLocation) {
        return breakpoints.contains(memLocation);
    }

    /**
     * Add new CPU listener to the list of stateObservers. CPU listener is an
     * implementation object of CPUListener interface. The methods are
     * called when some events are occured on CPU.
     *
     * @param listener CPUListener object
     * @return true if the listener was added, false otherwise
     */
    @Override
    public boolean addCPUListener(CPUListener listener) {
        return stateObservers.add(listener);
    }

    /**
     * Remove CPU listener object from the list of stateObservers. If the listener
     * is not included in the list, nothing will be done.
     *
     * @param listener CPUListener object
     * @return true if the listener was return, false otherwise
     */
    @Override
    public boolean removeCPUListener(CPUListener listener) {
        return stateObservers.remove(listener);
    }

    private void stopExecutor(ExecutorService executor) {
        Objects.requireNonNull(executor);
        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void destroy() {
        if (isDestroyed.compareAndSet(false, true)) {
            try {
                stop();
                stopExecutor(eventReceiver);
                stopExecutor(cpuExecutor);
                stopExecutor(cpuStoppedWatcher);
                stateObservers.clear();
            } finally {
                destroyInternal();
            }
        }
    }

    /**
     * Called by original destroy() method. Do not override the original destroy() method. Subsequent calls of
     * destroy() will call this only once.
     */
    protected abstract void destroyInternal();

    private void waitForFuture(Future future) {
        Objects.requireNonNull(future);
        try {
            future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            LOGGER.error("Unexpected error", e);
        }
    }

    private void notifyStateChanged() {
        final RunState tmpRunState = runState;
        stateObservers.forEach(observer -> {
            try {
                observer.runStateChanged(tmpRunState);
                observer.internalStateChanged();
            } catch (Exception e) {
                LOGGER.error("CPU Listener error", e);
            }
        });
    }

    private void ensureCpuIsStopped() {
        try {
            cpuStoppedWatcher.submit(EMPTY_TASK).get();
        } catch (ExecutionException e) {
            LOGGER.error("Unexpected error while waiting for CPU stop", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void reset() { reset(0); }

    @Override
    public void reset(int addr) {
        Future future = eventReceiver.submit(() -> {
            requestStop();
            ensureCpuIsStopped();
            resetInternal(addr);
            runState = RunState.STATE_STOPPED_BREAK;
            notifyStateChanged();
        });
        waitForFuture(future);
    }

    @Override
    public void execute() {
        Future future = eventReceiver.submit(() -> {
            if (runState == RunState.STATE_STOPPED_BREAK) {
                runState = RunState.STATE_RUNNING;
                notifyStateChanged();

                Future<RunState> cpuFuture = cpuExecutor.submit(AbstractCPU.this);
                cpuWatchTask = new CPUWatchTask(cpuFuture);
                cpuStoppedWatcher.submit(cpuWatchTask);
            }
        });
        waitForFuture(future);
    }

    @Override
    public void pause() {
        Future future = eventReceiver.submit(() -> {
            if (runState == RunState.STATE_RUNNING) {
                requestStop();
                ensureCpuIsStopped();
                if (runState == RunState.STATE_RUNNING || runState == RunState.STATE_STOPPED_NORMAL) {
                    runState = RunState.STATE_STOPPED_BREAK;
                }
                notifyStateChanged();
            }
        });
        waitForFuture(future);
    }

    @Override
    public void stop() {
        Future future = eventReceiver.submit(() -> {
            if (runState == RunState.STATE_STOPPED_BREAK || runState == RunState.STATE_RUNNING) {
                requestStop();
                ensureCpuIsStopped();
                if (runState == RunState.STATE_RUNNING || runState == RunState.STATE_STOPPED_BREAK) {
                    runState = RunState.STATE_STOPPED_NORMAL;
                }
                notifyStateChanged();
            }

        });
        waitForFuture(future);
    }

    @Override
    public void step() {
        Future future = eventReceiver.submit(() -> {
            if (runState == RunState.STATE_STOPPED_BREAK) {
                try {
                    runState = stepInternal();
                    if (runState == RunState.STATE_RUNNING) {
                        runState = RunState.STATE_STOPPED_BREAK;
                    }
                } catch (IndexOutOfBoundsException e) {
                    runState = RunState.STATE_STOPPED_ADDR_FALLOUT;
                    LOGGER.error("Unexpected error during emulation", e);
                } catch (Exception e) {
                    if (e.getCause() != null && e.getCause() instanceof IndexOutOfBoundsException) {
                        runState = RunState.STATE_STOPPED_ADDR_FALLOUT;
                    } else {
                        runState = RunState.STATE_STOPPED_BAD_INSTR;
                    }
                    LOGGER.error("Unexpected error during emulation", e);
                }
                notifyStateChanged();
            }
        });
        waitForFuture(future);
    }

    /**
     * Request CPU implementation about stopping the execution loop.
     * CONTRACT: run in event thread
     */
    private void requestStop() {
        CPUWatchTask tmpCpuWatchTask = cpuWatchTask;
        if (tmpCpuWatchTask != null) {
            tmpCpuWatchTask.requestStop();
        }
    }

    /**
     * Perform one emulation step in synchronized context.
     *
     * @return new CPU state. If nothing bad happened, it should return RunState.STATE_STOPPED_BREAK.
     * @throws Exception The emulator is allowed to throw any exception
     */
    protected abstract RunState stepInternal() throws Exception;

    /**
     * Performs specific CPU reset.
     *
     * CONTRACT: If this method throws an exception, the behavior is undefined.
     *
     * @param startPos starting position (similar to calling <code>reset(pos)</code>)
     */
    protected abstract void resetInternal(int startPos);

}
