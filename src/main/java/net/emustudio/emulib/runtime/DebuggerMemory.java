/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime;

/**
 * Read-only debugger information at CPU-visible addresses.
 * Implementations must be safe to query from any thread.
 */
public interface DebuggerMemory {
    /** Returns the label at this location, or an empty string when none exists. */
    String label(int address);

    /** Returns the comment at this location, or an empty string when none exists. */
    String comment(int address);

    /** Returns whether this location is the current instruction. */
    boolean isCurrentInstruction(int address);

    /** Registers a listener invoked when debugger information changes, possibly off the Swing thread. */
    void addListener(Runnable listener);

    /** Unregisters a previously registered listener. */
    void removeListener(Runnable listener);
}
