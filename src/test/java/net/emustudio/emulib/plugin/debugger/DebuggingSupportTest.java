package net.emustudio.emulib.plugin.debugger;

import net.emustudio.emulib.plugin.cpu.DecodedInstruction;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import static org.junit.Assert.*;

public class DebuggingSupportTest {
    private static final DecodedInstruction INSTRUCTION = DecodedInstruction.of(7, 1, "jump", "C3");

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

    private static boolean end(DebuggingSupport support, boolean completed, int nextAddress) {
        return support.end(completed, Optional.of(INSTRUCTION), OptionalInt.of(nextAddress));
    }

    private static StepView view() {
        return new StepView("jump", "jump", OptionalInt.of(7), List.of());
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
