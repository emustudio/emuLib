/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugin.cpu;

import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.*;

public class AbstractCPUContextTest {
    private final AbstractCPUContext context = new AbstractCPUContext() {
        @Override public boolean isPassedCyclesSupported() { return false; }
    };

    @Test
    public void gettersExposeLatestCPUState() {
        assertEquals(-1, context.getInstructionLocation());
        assertTrue(Float.isNaN(context.getCpuFrequencyKhz()));

        context.fireStateChanged(CPU.RunState.STATE_RUNNING, 20, 2000);

        assertEquals(20, context.getInstructionLocation());
        assertEquals(CPU.RunState.STATE_RUNNING, context.getRunState());
        assertEquals(2000, context.getCpuFrequencyKhz(), 0);
        assertThrows(NullPointerException.class, () -> context.fireStateChanged(null, 0, Float.NaN));
    }

    @Test
    public void internalChangesNotifyImmediatelyWithoutRunStateChanges() {
        Listener listener = new Listener();
        context.addCPUListener(listener);

        context.fireStateChanged(CPU.RunState.STATE_STOPPED_BREAK, 0, Float.NaN);
        context.fireStateChanged(CPU.RunState.STATE_STOPPED_BREAK, 0, Float.NaN);

        assertEquals(2, listener.internalChanges);
        assertSame(Thread.currentThread(), listener.thread);
        assertTrue(listener.states.isEmpty());
    }

    @Test
    public void runStateChangesCarryTheCPUState() {
        Listener listener = new Listener();
        context.addCPUListener(listener);

        context.fireStateChanged(CPU.RunState.STATE_RUNNING, 0, Float.NaN);
        context.fireStateChanged(CPU.RunState.STATE_STOPPED_BREAK, 0, Float.NaN);

        assertEquals(List.of(CPU.RunState.STATE_RUNNING, CPU.RunState.STATE_STOPPED_BREAK), listener.states);
        assertEquals(2, listener.internalChanges);
    }

    @Test
    public void removedListenerReceivesNoMoreEvents() {
        Listener listener = new Listener();
        context.addCPUListener(listener);
        context.fireStateChanged(CPU.RunState.STATE_STOPPED_BREAK, 0, Float.NaN);

        context.removeCPUListener(listener);
        context.fireStateChanged(CPU.RunState.STATE_STOPPED_BREAK, 0, Float.NaN);
        context.fireStateChanged(CPU.RunState.STATE_RUNNING, 0, Float.NaN);

        assertEquals(1, listener.internalChanges);
        assertTrue(listener.states.isEmpty());
    }

    @Test
    public void failingListenerDoesNotInterruptOtherListenersOrEdits() {
        context.addCPUListener(new CPUContext.CPUListener() {
            @Override public void runStateChanged(CPU.RunState state) { throw new IllegalStateException("broken view"); }
            @Override public void internalStateChanged() { throw new IllegalStateException("broken view"); }
        });
        Listener listener = new Listener();
        context.addCPUListener(listener);

        context.fireStateChanged(CPU.RunState.STATE_RUNNING, 0, Float.NaN);

        assertEquals(List.of(CPU.RunState.STATE_RUNNING), listener.states);
        assertEquals(1, listener.internalChanges);
    }

    private static class Listener implements CPUContext.CPUListener {
        final List<CPU.RunState> states = new ArrayList<>();
        int internalChanges;
        Thread thread;
        @Override public void runStateChanged(CPU.RunState state) { states.add(state); }
        @Override public void internalStateChanged() { internalChanges++; thread = Thread.currentThread(); }
    }
}
