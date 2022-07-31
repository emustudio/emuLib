/*
 * This file is part of emuLib.
 *
 * Copyright (C) 2006-2020  Peter Jakubčo
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

import net.emustudio.emulib.plugins.annotations.PluginRoot;
import net.emustudio.emulib.runtime.ApplicationApi;
import net.emustudio.emulib.runtime.settings.PluginSettings;
import net.jcip.annotations.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
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

/**
 * Implements fundamental functionality useful for most of the CPU plugins. Features include:
 * <p>
 * - support of breakpoints
 * - thread safe controlling of run states
 * - managing CPU listeners
 */
@ThreadSafe
@SuppressWarnings("unused")
public abstract class AbstractCPU implements CPU, Callable<CPU.RunState> {
    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractCPU.class);
    private final static Runnable EMPTY_TASK = () -> {
    };

    private final AtomicBoolean isDestroyed = new AtomicBoolean();
    private final ExecutorService eventReceiver = Executors.newSingleThreadExecutor();

    private final ExecutorService cpuExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService cpuStoppedWatcher = Executors.newSingleThreadExecutor();

    private final Set<CPUListener> stateObservers = new CopyOnWriteArraySet<>();
    private final Set<Integer> breakpoints = new ConcurrentSkipListSet<>();

    // ** CONTRACT: set only in "eventReceiver" or "cpuWatchTask" in a non-concurrent way **
    private volatile RunState runState = RunState.STATE_STOPPED_NORMAL;
    // set only in "execute" event
    private volatile CPUWatchTask cpuWatchTask;
    // ** END OF CONTRACT **

    /**
     * Plugin ID assigned by emuStudio
     */
    protected final long pluginID;

    /**
     * emuStudio API.
     */
    protected final ApplicationApi applicationApi;

    /**
     * CPU custom settings.
     */
    protected final PluginSettings settings;


    private class CPUWatchTask implements Runnable {
        private final Future<RunState> cpuFuture;

        private CPUWatchTask(Future<RunState> cpuFuture) {
            this.cpuFuture = Objects.requireNonNull(cpuFuture);
        }

        @Override
        public void run() {
            RunState originalRunState = runState;
            RunState tmpRunState = originalRunState;
            try {
                tmpRunState = cpuFuture.get();
            } catch (ExecutionException e) {
                if (e.getCause() instanceof IndexOutOfBoundsException) {
                    tmpRunState = RunState.STATE_STOPPED_ADDR_FALLOUT;
                } else {
                    Throwable cause = e.getCause().getCause();
                    if (cause instanceof IndexOutOfBoundsException) {
                        tmpRunState = RunState.STATE_STOPPED_ADDR_FALLOUT;
                    } else {
                        tmpRunState = RunState.STATE_STOPPED_BAD_INSTR;
                    }
                }
                LOGGER.error("Unexpected error during emulation", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                runState = tmpRunState;
                if (originalRunState != tmpRunState) {
                    notifyStateChanged(tmpRunState);
                }
            }
        }

        void requestStop() {
            cpuFuture.cancel(true);
        }
    }

    /**
     * Creates new instance of CPU.
     *
     * @param pluginID plugin ID
     * @param applicationApi emuStudio API
     * @param settings plugin custom settings
     */
    public AbstractCPU(long pluginID, ApplicationApi applicationApi, PluginSettings settings) {
        this.pluginID = pluginID;
        this.applicationApi = Objects.requireNonNull(applicationApi);
        this.settings = Objects.requireNonNull(settings);
    }


    @Override
    public String getTitle() {
        return getClass().getAnnotation(PluginRoot.class).title();
    }

    /**
     * Does nothing. Should be overridden.
     */
    @Override
    public void showSettings(JFrame parent) {

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
    public void setBreakpoint(int location) {
        breakpoints.add(location);
    }

    @Override
    public void unsetBreakpoint(int location) {
        breakpoints.remove(location);
    }

    @Override
    public boolean isBreakpointSet(int location) {
        return breakpoints.contains(location);
    }

    /**
     * Add new CPU listener to the list of stateObservers. CPU listener is an
     * implementation object of CPUListener interface. The methods are
     * called when some events are occured on CPU.
     *
     * @param listener CPUListener object
     */
    @Override
    public void addCPUListener(CPUListener listener) {
        stateObservers.add(listener);
    }

    /**
     * Remove CPU listener object from the list of stateObservers. If the listener
     * is not included in the list, nothing will be done.
     *
     * @param listener CPUListener object
     */
    @Override
    public void removeCPUListener(CPUListener listener) {
        stateObservers.remove(listener);
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

    private void waitForFuture(Future<?> future) {
        Objects.requireNonNull(future);
        try {
            future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            LOGGER.error("Unexpected error", e);
        }
    }

    private void notifyStateChanged(RunState tmpRunState) {
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
    public void reset() {
        reset(0);
    }

    @Override
    public void reset(int location) {
        Future<?> future = eventReceiver.submit(() -> {
            requestStop();
            ensureCpuIsStopped();
            resetInternal(location);
            RunState tmpRunState = RunState.STATE_STOPPED_BREAK;
            runState = tmpRunState;
            notifyStateChanged(tmpRunState);
        });
        waitForFuture(future);
    }

    @Override
    public void execute() {
        Future<?> future = eventReceiver.submit(() -> {
            if (runState == RunState.STATE_STOPPED_BREAK) {
                RunState tmpRunState = RunState.STATE_RUNNING;
                runState = tmpRunState;
                notifyStateChanged(tmpRunState);

                Future<RunState> cpuFuture = cpuExecutor.submit(AbstractCPU.this);
                cpuWatchTask = new CPUWatchTask(cpuFuture);
                cpuStoppedWatcher.submit(cpuWatchTask);
            }
        });
        waitForFuture(future);
    }

    @Override
    public void pause() {
        Future<?> future = eventReceiver.submit(() -> {
            if (runState == RunState.STATE_RUNNING) {
                requestStop();
                ensureCpuIsStopped();
                RunState tmpRunState = runState;

                if (tmpRunState == RunState.STATE_RUNNING || tmpRunState == RunState.STATE_STOPPED_NORMAL) {
                    tmpRunState = RunState.STATE_STOPPED_BREAK;
                }

                runState = tmpRunState;
                notifyStateChanged(tmpRunState);
            }
        });
        waitForFuture(future);
    }

    @Override
    public void stop() {
        Future<?> future = eventReceiver.submit(() -> {
            RunState tmpRunState = runState;

            if (tmpRunState == RunState.STATE_STOPPED_BREAK || tmpRunState == RunState.STATE_RUNNING) {
                requestStop();
                ensureCpuIsStopped();
                tmpRunState = runState;
                if (tmpRunState == RunState.STATE_RUNNING || tmpRunState == RunState.STATE_STOPPED_BREAK) {
                    tmpRunState = RunState.STATE_STOPPED_NORMAL;
                }
                runState = tmpRunState;
                notifyStateChanged(tmpRunState);
            }

        });
        waitForFuture(future);
    }

    @Override
    public void step() {
        Future<?> future = eventReceiver.submit(() -> {
            if (runState == RunState.STATE_STOPPED_BREAK) {
                RunState tmpRunState = RunState.STATE_STOPPED_ADDR_FALLOUT;
                try {
                    tmpRunState = stepInternal();
                    if (tmpRunState == RunState.STATE_RUNNING) {
                        tmpRunState = RunState.STATE_STOPPED_BREAK;
                    }
                } catch (IndexOutOfBoundsException e) {
                    LOGGER.error("Unexpected error during emulation", e);
                } catch (Exception e) {
                    if (!(e.getCause() instanceof IndexOutOfBoundsException)) {
                        tmpRunState = RunState.STATE_STOPPED_BAD_INSTR;
                    }
                    LOGGER.error("Unexpected error during emulation", e);
                } finally {
                    runState = tmpRunState;
                    notifyStateChanged(tmpRunState);
                }
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
     * <p>
     * CONTRACT: If this method throws an exception, the behavior is undefined.
     *
     * @param startPos starting position (similar to calling <code>reset(pos)</code>)
     */
    protected abstract void resetInternal(int startPos);

}
