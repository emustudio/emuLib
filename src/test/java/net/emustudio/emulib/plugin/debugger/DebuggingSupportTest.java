package net.emustudio.emulib.plugin.debugger;

import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

public class DebuggingSupportTest {
    @Test public void inactiveStepsDoNotCapturePresentation() {
        DebuggingSupport support = support(() -> { throw new AssertionError("Inactive capture"); },
                () -> { throw new AssertionError("Inactive capture"); });

        assertTrue(support.begin());
        assertFalse(support.end(true));

    }

    @Test public void beforeStopPreventsCaptureAndAnExecutedStepNotification() {
        int[] after = {0};
        DebuggingSupport support = support(() -> { throw new AssertionError("Refused capture"); },
                () -> { throw new AssertionError("Refused capture"); });
        support.observe(new Debugging.Observer() {
            public boolean beforeStep() { return true; }
            public boolean afterStep(boolean completed, Optional<StepView> view) { after[0]++; return false; }
        }, true);

        assertFalse(support.begin());
        assertFalse(support.end(true));

        assertEquals(0, after[0]);
    }

    @Test public void captureFailureLeavesObserverAndCompletionTruthIntact() {
        int[] attempts = {0};
        List<String> failures = new ArrayList<>();
        List<Boolean> completions = new ArrayList<>();
        DebuggingSupport support = support(() -> attempts[0]++, () -> { throw new IllegalArgumentException("broken format"); });
        support.observe(new Debugging.Observer() {
            public boolean beforeStep() { return false; }
            public boolean afterStep(boolean completed, Optional<StepView> view) {
                completions.add(completed);
                assertTrue(view.isEmpty());
                return false;
            }
            public void failed(String message) { failures.add(message); }
        }, true);

        assertTrue(support.begin());
        assertFalse(support.end(false));
        assertTrue(support.begin());
        assertFalse(support.end(true));

        assertEquals(List.of(false, true), completions);
        assertEquals(1, attempts[0]);
        assertEquals(1, failures.size());
        assertTrue(failures.get(0).contains("broken format"));
        assertTrue(support.isActive());
        assertFalse(support.isCapturing());
    }

    @Test public void oldHandleCannotDetachReinstalledObserver() throws Exception {
        DebuggingSupport support = support(() -> { }, () -> new StepView("s", "", OptionalInt.empty(), List.of()));
        Debugging.Observer observer = new Debugging.Observer() {
            public boolean beforeStep() { return false; }
            public boolean afterStep(boolean completed, Optional<StepView> capture) { return true; }
        };
        AutoCloseable old = support.observe(observer, false);
        old.close();
        AutoCloseable current = support.observe(observer, false);

        old.close();

        assertTrue(support.isActive());
        assertTrue(support.begin());
        assertTrue(support.end(true));
        current.close();
        assertFalse(support.isActive());
    }

    private static DebuggingSupport support(Runnable before, java.util.function.Supplier<StepView> after) {
        return new DebuggingSupport(before, after, active -> { });
    }
}
