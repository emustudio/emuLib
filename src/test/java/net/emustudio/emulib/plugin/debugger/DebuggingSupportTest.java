package net.emustudio.emulib.plugin.debugger;

import net.emustudio.emulib.plugin.cpu.DecodedInstruction;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class DebuggingSupportTest {
    private static final DecodedInstruction INSTRUCTION = DecodedInstruction.of(7, 1, "jump", "C3");
    private static final ExecutedBlock BLOCK = new ExecutedBlock(List.of(INSTRUCTION),
            List.of(new ExecutedBlock.Transition(7, 100)), 7, 100);

    @Test
    public void batchingRequiresExplicitObserverOptIn() {
        Observer observer = new Observer();
        DebuggingSupport support = new DebuggingSupport(() -> {}, () -> {}, DebuggingSupportTest::view, active -> {});
        support.observe(observer, Debugging.Capture.INSTRUCTION);

        assertFalse(observer.canBatchInstructions());
        assertThrows(UnsupportedOperationException.class, () -> observer.afterBlock(BLOCK));
        assertFalse(support.canBatchInstructions());
        assertTrue(support.begin());
        assertFalse(support.canBatchInstructions());
        assertFalse(end(support, true, 100));
        assertEquals(List.of(true), observer.completions);
    }

    @Test
    public void batchingOnlyAcceptsCapturedInstructionModeAndEndsTheStepOnce() throws Exception {
        for (Debugging.Capture mode : Debugging.Capture.values()) {
            DebuggingSupport support = new DebuggingSupport(() -> {}, () -> {}, DebuggingSupportTest::view, active -> {});
            BatchObserver observer = new BatchObserver();
            AutoCloseable handle = support.observe(observer, mode);

            assertTrue(support.begin());
            if (mode == Debugging.Capture.INSTRUCTION) {
                assertTrue(support.canBatchInstructions());
                support.endBlock(BLOCK);
                support.endBlock(BLOCK);
                assertEquals(List.of(BLOCK), observer.blocks);
                assertTrue(observer.completions.isEmpty());
                assertFalse(end(support, true, 100));
            } else {
                assertFalse(support.canBatchInstructions());
                assertTrue(observer.blocks.isEmpty());
                assertFalse(end(support, true, 100));
                assertEquals(List.of(true), observer.completions);
            }
            assertEquals(mode == Debugging.Capture.INSTRUCTION ? 1 : 0, observer.batchQueries);
            assertEquals(1, observer.beforeCalls);
            assertFalse(support.canBatchInstructions());
            handle.close();
        }
    }

    @Test
    public void refusedOrUncapturedInstructionCannotAuthorizeABlock() {
        for (boolean stopBefore : List.of(false, true)) {
            BatchObserver observer = new BatchObserver();
            observer.stopBefore = stopBefore;
            DebuggingSupport support = new DebuggingSupport(() -> { throw new IllegalStateException("Missing instruction"); },
                    () -> {}, DebuggingSupportTest::view, active -> {});
            support.observe(observer, Debugging.Capture.INSTRUCTION);

            assertEquals(!stopBefore, support.begin());
            assertFalse(support.canBatchInstructions());
            support.endBlock(BLOCK);
            assertTrue(observer.blocks.isEmpty());
            assertEquals(0, observer.batchQueries);
        }
    }

    @Test
    public void batchingQueryFailureFallsBackToIndividualInstructionObservation() {
        BatchObserver observer = new BatchObserver() {
            @Override public boolean canBatchInstructions() { throw new IllegalStateException("Broken batching query"); }
        };
        DebuggingSupport support = new DebuggingSupport(() -> {}, () -> {}, DebuggingSupportTest::view, active -> {});
        support.observe(observer, Debugging.Capture.INSTRUCTION);

        assertTrue(support.begin());
        assertFalse(support.canBatchInstructions());
        assertTrue(support.isActive());
        assertFalse(end(support, true, 100));
        assertEquals(List.of(true), observer.completions);
        assertSame(INSTRUCTION, observer.instructions.get(0).orElseThrow());
        assertEquals(1, observer.failures.size());
        assertTrue(observer.failures.get(0).contains("Broken batching query"));
    }

    @Test
    public void authorizedBlockSurvivesSameObserverModeHandoffWithoutRichCapture() throws Exception {
        BatchObserver observer = new BatchObserver();
        DebuggingSupport support = new DebuggingSupport(() -> {}, () -> fail("Block has no before-state capture"),
                () -> { throw new AssertionError("Block has no rich presentation"); }, active -> {});
        AutoCloseable old = support.observe(observer, Debugging.Capture.INSTRUCTION);
        assertTrue(support.begin());
        assertTrue(support.canBatchInstructions());
        old.close();
        AutoCloseable handle = support.observe(observer, Debugging.Capture.FULL);

        support.endBlock(BLOCK);
        assertEquals(List.of(BLOCK), observer.blocks);
        assertEquals(1, observer.batchQueries);
        assertTrue(observer.views.isEmpty());
        assertTrue(support.isCapturing());
        old.close();
        assertTrue(support.isActive());
        handle.close();
    }

    @Test
    public void currentFullModeOrDifferentObserverForbidsBlockAuthorization() throws Exception {
        for (boolean same : List.of(false, true)) {
            BatchObserver observer = new BatchObserver(), replacement = same ? observer : new BatchObserver();
            DebuggingSupport support = new DebuggingSupport(() -> {}, () -> {}, DebuggingSupportTest::view, active -> {});
            AutoCloseable old = support.observe(observer, Debugging.Capture.INSTRUCTION);
            assertTrue(support.begin());
            old.close();
            AutoCloseable handle = support.observe(replacement, same ? Debugging.Capture.FULL : Debugging.Capture.INSTRUCTION);

            assertFalse(support.canBatchInstructions());
            assertEquals(0, observer.batchQueries);
            assertEquals(0, replacement.batchQueries);
            end(support, true, 100);
            assertEquals(same ? List.of(true) : List.of(), observer.completions);
            handle.close();
        }
    }

    @Test
    public void replacementObserverNeverReceivesAnAlreadyAuthorizedBlock() throws Exception {
        BatchObserver first = new BatchObserver(), replacement = new BatchObserver();
        DebuggingSupport support = new DebuggingSupport(() -> {}, () -> {}, DebuggingSupportTest::view, active -> {});
        AutoCloseable old = support.observe(first, Debugging.Capture.INSTRUCTION);
        assertTrue(support.begin());
        assertTrue(support.canBatchInstructions());
        old.close();
        AutoCloseable handle = support.observe(replacement, Debugging.Capture.INSTRUCTION);

        support.endBlock(BLOCK);
        assertTrue(first.blocks.isEmpty());
        assertTrue(replacement.blocks.isEmpty());
        assertTrue(support.begin());
        assertTrue(support.canBatchInstructions());
        support.endBlock(BLOCK);
        assertEquals(List.of(BLOCK), replacement.blocks);
        handle.close();
    }

    @Test
    public void blockCallbackFailureIsIsolatedAndDetachesOnlyItsOwnRegistration() throws Exception {
        for (boolean replace : List.of(false, true)) {
            DebuggingSupport support = new DebuggingSupport(() -> {}, () -> {}, DebuggingSupportTest::view, active -> {});
            AutoCloseable[] old = new AutoCloseable[1], handle = new AutoCloseable[1];
            BatchObserver replacement = new BatchObserver();
            BatchObserver first = new BatchObserver() {
                @Override public void afterBlock(ExecutedBlock block) {
                    if (replace) {
                        close(old[0]);
                        handle[0] = support.observe(replacement, Debugging.Capture.INSTRUCTION);
                    }
                    throw new IllegalStateException("Broken block callback");
                }
            };
            old[0] = support.observe(first, Debugging.Capture.INSTRUCTION);
            assertTrue(support.begin());
            assertTrue(support.canBatchInstructions());

            support.endBlock(BLOCK);
            assertEquals(replace, support.isActive());
            assertTrue(replacement.failures.isEmpty());
            assertEquals(replace ? 0 : 1, first.failures.size());
            if (replace) handle[0].close();
        }
    }

    @Test
    public void inactiveStepsDoNotCaptureInstructionsOrPresentation() {
        DebuggingSupport support = new DebuggingSupport(
                () -> fail("Inactive instruction capture"), () -> fail("Inactive state capture"),
                () -> { throw new AssertionError("Inactive presentation capture"); }, active -> fail("Inactive instrumentation"));

        assertTrue(support.begin());
        assertFalse(end(support, true, 100));
    }

    @Test
    public void instructionModeReportsActualFlowWithoutRichCaptureOrAccessInstrumentation() throws Exception {
        int[] instructions = {0};
        List<Boolean> activity = new ArrayList<>();
        Observer observer = new Observer();
        DebuggingSupport support = new DebuggingSupport(() -> instructions[0]++,
                () -> fail("Instruction mode captured registers"),
                () -> { throw new AssertionError("Instruction mode built presentation"); }, activity::add);
        AutoCloseable handle = support.observe(observer, Debugging.Capture.INSTRUCTION);

        assertTrue(support.begin());
        assertFalse(end(support, true, 100));

        assertEquals(1, instructions[0]);
        assertEquals(List.of(true), observer.completions);
        assertSame(INSTRUCTION, observer.instructions.get(0).orElseThrow());
        assertEquals(100, observer.nextAddresses.get(0).getAsInt());
        assertTrue(observer.views.get(0).isEmpty());
        assertTrue(support.isActive());
        assertFalse(support.isCapturing());
        assertFalse(support.requiresAccesses());
        handle.close();
        assertEquals(List.of(false, false), activity);
    }

    @Test
    public void accessModeEnablesGuestAccessTrackingWithoutRichCapture() throws Exception {
        int[] instructions = {0};
        List<Boolean> activity = new ArrayList<>();
        Observer observer = new Observer();
        DebuggingSupport support = new DebuggingSupport(() -> instructions[0]++,
                () -> fail("Access mode captured registers"),
                () -> { throw new AssertionError("Access mode built presentation"); }, activity::add);
        AutoCloseable handle = support.observe(observer, Debugging.Capture.ACCESSES);

        assertTrue(support.requiresAccesses());
        assertFalse(support.isCapturing());
        assertTrue(support.begin());
        end(support, false, 7);

        assertEquals(1, instructions[0]);
        assertEquals(List.of(false), observer.completions);
        assertEquals(7, observer.nextAddresses.get(0).getAsInt());
        assertTrue(observer.views.get(0).isEmpty());
        handle.close();
        assertEquals(List.of(true, false), activity);
    }

    @Test
    public void fullModeCapturesPresentationAlongsideInstructionAndResult() throws Exception {
        List<String> callbacks = new ArrayList<>();
        StepView view = view();
        Observer observer = new Observer();
        DebuggingSupport support = new DebuggingSupport(() -> callbacks.add("instruction"),
                () -> callbacks.add("state"), () -> { callbacks.add("presentation"); return view; }, active -> {});
        AutoCloseable handle = support.observe(observer, Debugging.Capture.FULL);

        assertTrue(support.isCapturing());
        assertTrue(support.requiresAccesses());
        assertTrue(support.begin());
        end(support, true, 100);

        assertEquals(List.of("instruction", "state", "presentation"), callbacks);
        assertSame(view, observer.views.get(0).orElseThrow());
        assertSame(INSTRUCTION, observer.instructions.get(0).orElseThrow());
        handle.close();
    }

    @Test
    public void beforeStopPreventsCaptureAndExecutedStepNotification() {
        Observer observer = new Observer();
        observer.stopBefore = true;
        DebuggingSupport support = new DebuggingSupport(
                () -> fail("Refused instruction capture"), () -> fail("Refused state capture"),
                () -> { throw new AssertionError("Refused presentation capture"); }, active -> {});
        support.observe(observer, Debugging.Capture.FULL);

        assertFalse(support.begin());
        assertFalse(end(support, true, 100));

        assertTrue(observer.completions.isEmpty());
    }

    @Test
    public void failedRichCaptureDowngradesToAccessesWithoutDroppingInstructionEvents() {
        for (boolean beforeFails : new boolean[] {true, false}) {
            int[] instructions = {0}, stateCaptures = {0}, presentations = {0};
            Observer observer = new Observer();
            DebuggingSupport support = new DebuggingSupport(() -> instructions[0]++, () -> {
                stateCaptures[0]++;
                if (beforeFails) throw new IllegalArgumentException("broken state");
            }, () -> {
                presentations[0]++;
                throw new IllegalArgumentException("broken format");
            }, active -> {});
            support.observe(observer, Debugging.Capture.FULL);

            assertTrue(support.begin());
            end(support, false, 7);
            assertTrue(support.begin());
            end(support, true, 100);

            assertEquals(2, instructions[0]);
            assertEquals(1, stateCaptures[0]);
            assertEquals(beforeFails ? 0 : 1, presentations[0]);
            assertEquals(List.of(false, true), observer.completions);
            assertSame(INSTRUCTION, observer.instructions.get(0).orElseThrow());
            assertSame(INSTRUCTION, observer.instructions.get(1).orElseThrow());
            assertTrue(observer.views.stream().allMatch(Optional::isEmpty));
            assertEquals(1, observer.failures.size());
            assertTrue(observer.failures.get(0).contains(beforeFails ? "broken state" : "broken format"));
            assertTrue(support.isActive());
            assertTrue(support.requiresAccesses());
            assertFalse(support.isCapturing());
        }
    }

    @Test
    public void instructionCaptureFailureReportsMissingFactsWithoutReusingStaleInstruction() {
        int[] attempts = {0};
        Observer observer = new Observer();
        DebuggingSupport support = new DebuggingSupport(() -> {
            if (++attempts[0] == 1) throw new IllegalStateException("instruction unavailable");
        }, () -> fail("Unexpected rich capture"), DebuggingSupportTest::view, active -> {});
        support.observe(observer, Debugging.Capture.INSTRUCTION);

        assertTrue(support.begin());
        end(support, false, 7);
        assertTrue(support.begin());
        end(support, true, 100);

        assertTrue(observer.instructions.get(0).isEmpty());
        assertSame(INSTRUCTION, observer.instructions.get(1).orElseThrow());
        assertEquals(List.of(false, true), observer.completions);
        assertEquals(1, observer.failures.size());
        assertTrue(observer.failures.get(0).contains("instruction unavailable"));
        assertTrue(support.isActive());
    }

    @Test
    public void missingDecodedInstructionAndNextAddressRemainExplicit() {
        Observer observer = new Observer();
        DebuggingSupport support = new DebuggingSupport(() -> {}, () -> {}, DebuggingSupportTest::view, active -> {});
        support.observe(observer, Debugging.Capture.INSTRUCTION);

        assertTrue(support.begin());
        support.end(false, Optional.empty(), OptionalInt.empty());

        assertEquals(List.of(false), observer.completions);
        assertTrue(observer.instructions.get(0).isEmpty());
        assertTrue(observer.nextAddresses.get(0).isEmpty());
    }

    @Test
    public void observerFailureDetachesAccessInstrumentationWithoutStoppingGuestExecution() {
        for (boolean beforeFails : new boolean[] {true, false}) {
            List<Boolean> activity = new ArrayList<>();
            Debugging.Observer observer = new Observer() {
                @Override public boolean beforeStep() {
                    if (beforeFails) throw new IllegalStateException("observer failed");
                    return false;
                }
                @Override public boolean afterStep(boolean completed, Optional<DecodedInstruction> instruction,
                                                   OptionalInt nextAddress, Optional<StepView> capture) {
                    throw new IllegalStateException("observer failed");
                }
            };
            DebuggingSupport support = new DebuggingSupport(() -> {}, () -> {}, DebuggingSupportTest::view, activity::add);
            support.observe(observer, Debugging.Capture.ACCESSES);

            assertTrue(support.begin());
            assertFalse(end(support, true, 100));

            assertFalse(support.isActive());
            assertFalse(support.requiresAccesses());
            assertEquals(List.of(true, false), activity);
        }
    }

    @Test
    public void oldHandleCannotDetachReinstalledObserver() throws Exception {
        DebuggingSupport support = new DebuggingSupport(() -> {}, () -> {}, DebuggingSupportTest::view, active -> {});
        Observer observer = new Observer();
        observer.stopAfter = true;
        AutoCloseable old = support.observe(observer, Debugging.Capture.INSTRUCTION);
        old.close();
        AutoCloseable current = support.observe(observer, Debugging.Capture.INSTRUCTION);

        old.close();

        assertTrue(support.isActive());
        assertTrue(support.begin());
        assertTrue(end(support, true, 100));
        current.close();
        assertFalse(support.isActive());
    }

    @Test
    public void attachingDuringInactiveStepStartsFullCaptureAtNextBoundary() throws Exception {
        List<String> captures = new ArrayList<>();
        DebuggingSupport support = new DebuggingSupport(() -> captures.add("instruction"),
                () -> captures.add("before"), () -> { captures.add("after"); return view(); }, active -> {});
        Observer observer = new Observer();
        assertTrue(support.begin());
        AutoCloseable handle = support.observe(observer, Debugging.Capture.FULL);

        assertFalse(end(support, true, 100));
        assertTrue(captures.isEmpty());
        assertTrue(observer.completions.isEmpty());
        assertTrue(support.begin());
        assertFalse(end(support, true, 100));
        assertEquals(List.of("instruction", "before", "after"), captures);
        assertEquals(List.of(true), observer.completions);
        assertTrue(observer.views.get(0).isPresent());
        handle.close();
    }

    @Test
    public void replacementCannotInheritInstructionOrPartialFullCapture() throws Exception {
        AtomicInteger states = new AtomicInteger(), presentations = new AtomicInteger();
        DebuggingSupport support = new DebuggingSupport(() -> {}, states::incrementAndGet,
                () -> { presentations.incrementAndGet(); return view(); }, active -> {});
        Observer first = new Observer(), replacement = new Observer();
        AutoCloseable old = support.observe(first, Debugging.Capture.INSTRUCTION);
        assertTrue(support.begin());
        old.close();
        AutoCloseable handle = support.observe(replacement, Debugging.Capture.FULL);

        assertFalse(end(support, true, 100));
        assertTrue(first.completions.isEmpty());
        assertTrue(replacement.completions.isEmpty());
        assertEquals(0, presentations.get());
        assertTrue(support.begin());
        assertFalse(end(support, true, 100));
        assertEquals(1, states.get());
        assertEquals(1, presentations.get());
        assertEquals(List.of(true), replacement.completions);
        old.close();
        assertTrue(support.isActive());
        handle.close();
    }

    @Test
    public void sameObserverModeChangesKeepInFlightFactsWithoutPartialPresentation() throws Exception {
        for (Debugging.Capture before : List.of(Debugging.Capture.INSTRUCTION, Debugging.Capture.FULL)) {
            Debugging.Capture after = before == Debugging.Capture.FULL ? Debugging.Capture.INSTRUCTION : Debugging.Capture.FULL;
            AtomicInteger states = new AtomicInteger(), presentations = new AtomicInteger();
            DebuggingSupport support = new DebuggingSupport(() -> {}, states::incrementAndGet,
                    () -> { presentations.incrementAndGet(); return view(); }, active -> {});
            Observer observer = new Observer();
            AutoCloseable old = support.observe(observer, before);
            assertTrue(support.begin());
            old.close();
            AutoCloseable handle = support.observe(observer, after);

            assertFalse(end(support, true, 100));
            assertEquals(List.of(true), observer.completions);
            assertSame(INSTRUCTION, observer.instructions.get(0).orElseThrow());
            assertEquals(100, observer.nextAddresses.get(0).getAsInt());
            assertTrue(observer.views.get(0).isEmpty());
            assertEquals(0, presentations.get());
            assertTrue(support.begin());
            assertFalse(end(support, true, 101));
            assertEquals(List.of(true, true), observer.completions);
            assertEquals(after == Debugging.Capture.FULL, observer.views.get(1).isPresent());
            assertEquals(1, states.get());
            assertEquals(after == Debugging.Capture.FULL ? 1 : 0, presentations.get());
            old.close();
            assertTrue(support.isActive());
            handle.close();
        }
    }

    @Test
    public void sameObserverModeChangeInsideBeforeStepKeepsInstructionAndCompleteNewCapture() throws Exception {
        for (Debugging.Capture before : List.of(Debugging.Capture.INSTRUCTION, Debugging.Capture.FULL)) {
            Debugging.Capture after = before == Debugging.Capture.FULL ? Debugging.Capture.INSTRUCTION : Debugging.Capture.FULL;
            AtomicInteger states = new AtomicInteger(), presentations = new AtomicInteger(), beforeCalls = new AtomicInteger();
            AtomicBoolean change = new AtomicBoolean(true);
            DebuggingSupport support = new DebuggingSupport(() -> {}, states::incrementAndGet,
                    () -> { presentations.incrementAndGet(); return view(); }, active -> {});
            AutoCloseable[] old = new AutoCloseable[1], handle = new AutoCloseable[1];
            Observer observer = new Observer() {
                @Override public boolean beforeStep() {
                    beforeCalls.incrementAndGet();
                    if (change.getAndSet(false)) {
                        close(old[0]);
                        handle[0] = support.observe(this, after);
                    }
                    return false;
                }
            };
            old[0] = support.observe(observer, before);

            assertTrue(support.begin());
            assertFalse(end(support, true, 100));
            assertEquals(1, beforeCalls.get());
            assertEquals(List.of(true), observer.completions);
            assertSame(INSTRUCTION, observer.instructions.get(0).orElseThrow());
            assertEquals(100, observer.nextAddresses.get(0).getAsInt());
            assertEquals(after == Debugging.Capture.FULL, observer.views.get(0).isPresent());
            assertEquals(after == Debugging.Capture.FULL ? 1 : 0, states.get());
            assertEquals(states.get(), presentations.get());
            old[0].close();
            assertTrue(support.isActive());
            handle[0].close();
        }
    }

    @Test
    public void sameObserverReplacementInsideInstructionCaptureCannotInventBeforeState() throws Exception {
        Observer observer = new Observer();
        AutoCloseable[] old = new AutoCloseable[1], handle = new AutoCloseable[1];
        DebuggingSupport[] support = new DebuggingSupport[1];
        AtomicBoolean change = new AtomicBoolean(true);
        AtomicInteger states = new AtomicInteger(), presentations = new AtomicInteger();
        support[0] = new DebuggingSupport(() -> {
            if (change.getAndSet(false)) {
                close(old[0]);
                handle[0] = support[0].observe(observer, Debugging.Capture.FULL);
            }
        }, states::incrementAndGet, () -> { presentations.incrementAndGet(); return view(); }, active -> {});
        old[0] = support[0].observe(observer, Debugging.Capture.FULL);

        assertTrue(support[0].begin());
        assertFalse(end(support[0], true, 100));
        assertEquals(List.of(true), observer.completions);
        assertSame(INSTRUCTION, observer.instructions.get(0).orElseThrow());
        assertEquals(100, observer.nextAddresses.get(0).getAsInt());
        assertTrue(observer.views.get(0).isEmpty());
        assertEquals(0, states.get());
        assertEquals(0, presentations.get());
        assertTrue(support[0].begin());
        assertFalse(end(support[0], true, 101));
        assertTrue(observer.views.get(1).isPresent());
        assertEquals(1, states.get());
        assertEquals(1, presentations.get());
        handle[0].close();
    }

    @Test
    public void sameObserverReplacementInsideAfterCaptureKeepsFactsButDiscardsOldView() throws Exception {
        for (Debugging.Capture after : List.of(Debugging.Capture.INSTRUCTION, Debugging.Capture.FULL)) {
            Observer observer = new Observer();
            AutoCloseable[] old = new AutoCloseable[1], handle = new AutoCloseable[1];
            DebuggingSupport[] support = new DebuggingSupport[1];
            AtomicBoolean change = new AtomicBoolean(true);
            support[0] = new DebuggingSupport(() -> {}, () -> {}, () -> {
                if (change.getAndSet(false)) {
                    close(old[0]);
                    handle[0] = support[0].observe(observer, after);
                }
                return view();
            }, active -> {});
            old[0] = support[0].observe(observer, Debugging.Capture.FULL);

            assertTrue(support[0].begin());
            assertFalse(end(support[0], true, 100));
            assertEquals(List.of(true), observer.completions);
            assertSame(INSTRUCTION, observer.instructions.get(0).orElseThrow());
            assertEquals(100, observer.nextAddresses.get(0).getAsInt());
            assertTrue(observer.views.get(0).isEmpty());
            assertTrue(support[0].begin());
            assertFalse(end(support[0], true, 101));
            assertEquals(after == Debugging.Capture.FULL, observer.views.get(1).isPresent());
            handle[0].close();
        }
    }

    @Test
    public void oldObserverFailureCannotDetachReplacement() throws Exception {
        DebuggingSupport support = new DebuggingSupport(() -> {}, () -> {}, DebuggingSupportTest::view, active -> {});
        AutoCloseable[] old = new AutoCloseable[1], handle = new AutoCloseable[1];
        Observer replacement = new Observer();
        Observer first = new Observer() {
            @Override public boolean afterStep(boolean completed, Optional<DecodedInstruction> instruction,
                                               OptionalInt nextAddress, Optional<StepView> capture) {
                close(old[0]);
                handle[0] = support.observe(replacement, Debugging.Capture.INSTRUCTION);
                throw new IllegalStateException("Old observer failed");
            }
        };
        old[0] = support.observe(first, Debugging.Capture.INSTRUCTION);

        assertTrue(support.begin());
        assertFalse(end(support, true, 100));
        assertTrue(support.isActive());
        assertTrue(replacement.failures.isEmpty());
        assertTrue(support.begin());
        assertFalse(end(support, true, 100));
        assertEquals(List.of(true), replacement.completions);
        handle[0].close();
    }

    @Test
    public void liveAttachIsVisibleToInactiveExecutionThread() throws Exception {
        CountDownLatch started = new CountDownLatch(1), observed = new CountDownLatch(1);
        AtomicBoolean stop = new AtomicBoolean();
        DebuggingSupport support = new DebuggingSupport(() -> {}, () -> {}, DebuggingSupportTest::view, active -> {});
        ExecutorService execution = Executors.newSingleThreadExecutor();
        try {
            Future<?> worker = execution.submit(() -> {
                started.countDown();
                while (!stop.get()) {
                    assertTrue(support.begin());
                    end(support, true, 100);
                }
            });
            assertTrue(started.await(2, TimeUnit.SECONDS));
            AutoCloseable handle = support.observe(new Observer() {
                @Override public boolean beforeStep() { observed.countDown(); return false; }
            }, Debugging.Capture.INSTRUCTION);
            assertTrue("Execution must see newly attached observer", observed.await(2, TimeUnit.SECONDS));
            stop.set(true);
            worker.get(2, TimeUnit.SECONDS);
            handle.close();
        } finally {
            stop.set(true);
            execution.shutdownNow();
        }
    }

    @Test
    public void firstFullStepWaitsForAccessInstrumentationWithoutHoldingObserverLock() throws Exception {
        CountDownLatch configuring = new CountDownLatch(1), configured = new CountDownLatch(1), entering = new CountDownLatch(1);
        AtomicBoolean ready = new AtomicBoolean();
        DebuggingSupport support = new DebuggingSupport(() -> assertTrue(ready.get()),
                () -> assertTrue(ready.get()), DebuggingSupportTest::view, active -> {
                    if (active) {
                        configuring.countDown();
                        await(configured);
                        ready.set(true);
                    }
                });
        ExecutorService threads = Executors.newFixedThreadPool(2);
        try {
            Future<AutoCloseable> handle = threads.submit(() -> support.observe(new Observer(), Debugging.Capture.FULL));
            assertTrue(configuring.await(2, TimeUnit.SECONDS));
            Future<Boolean> begin = threads.submit(() -> { entering.countDown(); return support.begin(); });
            assertTrue(entering.await(2, TimeUnit.SECONDS));
            assertThrows(TimeoutException.class, () -> begin.get(100, TimeUnit.MILLISECONDS));
            configured.countDown();
            assertTrue(begin.get(2, TimeUnit.SECONDS));
            handle.get(2, TimeUnit.SECONDS).close();
        } finally {
            configured.countDown();
            threads.shutdownNow();
        }
    }

    @Test
    public void concurrentReattachCannotLeaveAccessInstrumentationDisabled() throws Exception {
        List<Boolean> activity = new ArrayList<>();
        CountDownLatch disabling = new CountDownLatch(1), disabled = new CountDownLatch(1);
        AtomicBoolean hold = new AtomicBoolean();
        DebuggingSupport support = new DebuggingSupport(() -> {}, () -> {}, DebuggingSupportTest::view, active -> {
            activity.add(active);
            if (!active && hold.get()) { disabling.countDown(); await(disabled); }
        });
        AutoCloseable first = support.observe(new Observer(), Debugging.Capture.FULL);
        ExecutorService thread = Executors.newSingleThreadExecutor();
        try {
            hold.set(true);
            Future<?> closing = thread.submit(() -> close(first));
            assertTrue(disabling.await(2, TimeUnit.SECONDS));
            AutoCloseable replacement = support.observe(new Observer(), Debugging.Capture.FULL);
            assertTrue(support.isActive());
            disabled.countDown();
            closing.get(2, TimeUnit.SECONDS);
            assertEquals(List.of(true, false, true), activity);
            assertTrue(support.requiresAccesses());
            replacement.close();
            assertFalse(support.requiresAccesses());
        } finally {
            disabled.countDown();
            thread.shutdownNow();
        }
    }

    private static void await(CountDownLatch latch) {
        try { latch.await(); }
        catch (InterruptedException failure) { Thread.currentThread().interrupt(); throw new AssertionError(failure); }
    }

    private static void close(AutoCloseable handle) {
        try { handle.close(); }
        catch (Exception failure) { throw new AssertionError(failure); }
    }

    private static boolean end(DebuggingSupport support, boolean completed, int nextAddress) {
        return support.end(completed, Optional.of(INSTRUCTION), OptionalInt.of(nextAddress));
    }

    private static StepView view() {
        return new StepView("jump", "jump", OptionalInt.of(7), List.of());
    }

    private static class BatchObserver extends Observer {
        final List<ExecutedBlock> blocks = new ArrayList<>();
        int beforeCalls, batchQueries;
        @Override public boolean beforeStep() { beforeCalls++; return super.beforeStep(); }
        @Override public boolean canBatchInstructions() { batchQueries++; return true; }
        @Override public void afterBlock(ExecutedBlock block) { blocks.add(block); }
    }

    private static class Observer implements Debugging.Observer {
        final List<Boolean> completions = new ArrayList<>();
        final List<Optional<DecodedInstruction>> instructions = new ArrayList<>();
        final List<OptionalInt> nextAddresses = new ArrayList<>();
        final List<Optional<StepView>> views = new ArrayList<>();
        final List<String> failures = new ArrayList<>();
        boolean stopBefore, stopAfter;

        @Override public boolean beforeStep() { return stopBefore; }
        @Override public boolean afterStep(boolean completed, Optional<DecodedInstruction> instruction,
                                           OptionalInt nextAddress, Optional<StepView> capture) {
            completions.add(completed);
            instructions.add(instruction);
            nextAddresses.add(nextAddress);
            views.add(capture);
            return stopAfter;
        }
        @Override public void failed(String message) { failures.add(message); }
    }
}
