/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.cpu;

import java.util.List;

/** Structured static flow. Empty targets for indirect jumps/calls; no guessed destinations. */
public final class InstructionFlow {
    public enum Kind {
        LINEAR,
        JUMP,
        CALL,
        RETURN,
        STOP
    }

    public final Kind kind;
    public final int next;
    public final boolean fallThrough;
    public final List<Integer> targets;

    public InstructionFlow(Kind kind, int next, boolean fallThrough, List<Integer> targets) {
        this.kind = kind;
        this.next = next;
        this.fallThrough = fallThrough;
        this.targets = List.copyOf(targets);
    }
}
