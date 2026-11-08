/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime;

import net.emustudio.emulib.plugins.cpu.CPU;
import java.util.concurrent.CompletableFuture;

/** Host-owned machine execution state, observations, and serialized paused edits. */
public interface ExecutionState {
    /** Returns the current CPU-visible instruction address, or -1 when unavailable. */
    int currentInstruction();

    CPU.RunState runState();

    /** True while a machine command is pending, including cooperative stop and close. */
    boolean isBusy();

    /** Executes a paused edit on the CPU worker; rejects running, busy, or stale requests. */
    CompletableFuture<Void> mutate(Runnable mutation);

    /** Observers are notified by the host, outside Swing and without lifecycle locks held. */
    void addCPUListener(CPU.CPUListener listener);

    void removeCPUListener(CPU.CPUListener listener);

    /** Registers a listener; notifications may occur outside the Swing thread. */
    void addListener(Runnable listener);

    /** Removes a previously registered listener. */
    void removeListener(Runnable listener);
}
