/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.cpu;

import net.emustudio.emulib.plugins.cpu.CPU.RunState;
import net.emustudio.emulib.runtime.ApplicationApi;
import net.emustudio.emulib.runtime.settings.PluginSettings;
import org.junit.After;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AbstractCPUExecutionTest {
    private final BlockingCPU implementation = new BlockingCPU();
    private final CPU cpu = implementation;
    private final ExecutorService caller = Executors.newSingleThreadExecutor();

    @After
    public void tearDown() throws InterruptedException {
        implementation.release.countDown();
        caller.shutdown();
        assertTrue(caller.awaitTermination(5, TimeUnit.SECONDS));
        cpu.destroy();
    }

    @Test
    public void pauseWaitsForExecutionToExit() throws Exception {
        assertWaitsForExecution(CPU::pause, RunState.STATE_STOPPED_BREAK, false);
    }

    @Test
    public void resetWaitsForExecutionToExit() throws Exception {
        assertWaitsForExecution(CPU::reset, RunState.STATE_STOPPED_BREAK, false);
        assertFalse(implementation.resetWhileExecuting);
    }

    @Test
    public void stopWaitsForExecutionToExit() throws Exception {
        assertWaitsForExecution(CPU::stop, RunState.STATE_STOPPED_NORMAL, false);
    }

    @Test
    public void interruptedPauseCallerStillWaitsAndKeepsInterruptFlag() throws Exception {
        assertWaitsForExecution(CPU::pause, RunState.STATE_STOPPED_BREAK, true);
    }

    private void assertWaitsForExecution(Consumer<CPU> operation, RunState expected, boolean interruptCaller)
            throws Exception {
        cpu.reset();
        List<RunState> states = new CopyOnWriteArrayList<>();
        AtomicBoolean stoppedWhileExecuting = new AtomicBoolean();
        cpu.addCPUListener(listener(state -> {
            states.add(state);
            if (state != RunState.STATE_RUNNING && implementation.executing) {
                stoppedWhileExecuting.set(true);
            }
        }));
        cpu.execute();
        assertTrue(implementation.entered.await(5, TimeUnit.SECONDS));
        AtomicBoolean interruptPreserved = new AtomicBoolean();
        Future<?> request = caller.submit(() -> {
            if (interruptCaller) {
                Thread.currentThread().interrupt();
            }
            operation.accept(cpu);
            interruptPreserved.set(Thread.currentThread().isInterrupted());
        });
        assertTrue(implementation.interrupted.await(5, TimeUnit.SECONDS));
        try {
            request.get(100, TimeUnit.MILLISECONDS);
            fail("CPU control returned while execution was still active");
        } catch (TimeoutException expectedTimeout) {
            // The in-flight instruction is deliberately held until the test releases it.
        }
        assertEquals(List.of(RunState.STATE_RUNNING), states);
        implementation.release.countDown();
        request.get(5, TimeUnit.SECONDS);
        assertEquals(List.of(RunState.STATE_RUNNING, expected), states);
        assertFalse(stoppedWhileExecuting.get());
        assertEquals(interruptCaller, interruptPreserved.get());
    }

    @Test
    public void naturalCompletionPublishesStateAfterExecutionExits() throws Exception {
        assertCompletion(RunState.STATE_STOPPED_NORMAL, null);
    }

    @Test
    public void failedExecutionPublishesFailureAfterExecutionExits() throws Exception {
        assertCompletion(RunState.STATE_STOPPED_BAD_INSTR, new IllegalStateException("CPU failure"));
    }

    private void assertCompletion(RunState expected, RuntimeException failure) throws Exception {
        implementation.failure = failure;
        cpu.reset();
        CountDownLatch stopped = new CountDownLatch(1);
        AtomicBoolean stoppedWhileExecuting = new AtomicBoolean();
        List<RunState> states = new CopyOnWriteArrayList<>();
        cpu.addCPUListener(listener(state -> {
            states.add(state);
            if (state != RunState.STATE_RUNNING) {
                stoppedWhileExecuting.set(implementation.executing);
                stopped.countDown();
            }
        }));
        cpu.execute();
        assertTrue(implementation.entered.await(5, TimeUnit.SECONDS));
        implementation.release.countDown();
        assertTrue(stopped.await(5, TimeUnit.SECONDS));
        assertEquals(List.of(RunState.STATE_RUNNING, expected), states);
        assertFalse(stoppedWhileExecuting.get());
    }

    @Test(timeout = 10000)
    public void immediatePauseAndCompletedExecutionDoNotLeavePendingWaiters() {
        implementation.release.countDown();
        implementation.setRunStateToReturn(RunState.STATE_STOPPED_BREAK);
        cpu.reset();
        for (int i = 0; i < 100; i++) {
            cpu.execute();
            cpu.pause();
        }
        cpu.reset();
    }

    private static CPU.CPUListener listener(Consumer<RunState> onState) {
        return new CPU.CPUListener() {
            @Override
            public void runStateChanged(RunState state) {
                onState.accept(state);
            }

            @Override
            public void internalStateChanged() {
            }
        };
    }

    private static <T> T niceMock(Class<T> type) {
        T mock = createNiceMock(type);
        replay(mock);
        return mock;
    }

    private static final class BlockingCPU extends AbstractCPUStub {
        private final CountDownLatch entered = new CountDownLatch(1);
        private final CountDownLatch interrupted = new CountDownLatch(1);
        private final CountDownLatch release = new CountDownLatch(1);
        private volatile boolean executing;
        private volatile boolean resetWhileExecuting;
        private RuntimeException failure;
        private RunState result = RunState.STATE_STOPPED_NORMAL;

        private BlockingCPU() {
            super(0, niceMock(ApplicationApi.class), niceMock(PluginSettings.class));
        }

        @Override
        public void setRunStateToReturn(RunState result) {
            this.result = result;
        }

        @Override
        public RunState call() {
            executing = true;
            entered.countDown();
            try {
                while (true) {
                    try {
                        release.await();
                        if (failure != null) {
                            throw failure;
                        }
                        return result;
                    } catch (InterruptedException e) {
                        interrupted.countDown();
                    }
                }
            } finally {
                executing = false;
            }
        }

        @Override
        protected void resetInternal(int startPos) {
            resetWhileExecuting = executing;
        }
    }
}
