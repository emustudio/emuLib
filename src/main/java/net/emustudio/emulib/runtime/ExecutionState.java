/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime;

/** Current CPU execution location and change notifications. */
public interface ExecutionState {
    /** Returns the current CPU-visible instruction address, or -1 when unavailable. */
    int currentInstruction();

    /** Registers a listener; notifications may occur outside the Swing thread. */
    void addListener(Runnable listener);

    /** Removes a previously registered listener. */
    void removeListener(Runnable listener);
}
