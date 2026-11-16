/* SPDX-FileCopyrightText: 2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugin.debugger;

import net.emustudio.emulib.plugin.cpu.DecodedInstruction;

import java.util.List;
import java.util.HashSet;
import java.util.Set;

/**
 * Immutable instruction and transition facts from an executed optimization block.
 * The producer supplies facts about actual execution; no transitions or instruction order are inferred.
 * Transition sources belong to the block, including the explicit final last-address to next-address edge.
 */
public final class ExecutedBlock {
    private final List<DecodedInstruction> instructions;
    private final List<Transition> transitions;
    private final int lastAddress;
    private final int nextAddress;

    public ExecutedBlock(List<DecodedInstruction> instructions, List<Transition> transitions,
                         int lastAddress, int nextAddress) {
        this.instructions = List.copyOf(instructions);
        this.transitions = List.copyOf(transitions);
        if (this.instructions.isEmpty()) throw new IllegalArgumentException("Executed block must contain instructions");
        Set<Integer> addresses = new HashSet<>();
        this.instructions.forEach(instruction -> addresses.add(instruction.address()));
        if (!addresses.contains(lastAddress))
            throw new IllegalArgumentException("Last executed address must be present in the block");
        if (this.transitions.stream().anyMatch(transition -> !addresses.contains(transition.from())))
            throw new IllegalArgumentException("Transition sources must be present in the block");
        if (this.transitions.stream().noneMatch(transition -> transition.from() == lastAddress && transition.to() == nextAddress))
            throw new IllegalArgumentException("Executed block must include its final transition");
        this.lastAddress = lastAddress;
        this.nextAddress = nextAddress;
    }

    public List<DecodedInstruction> instructions() { return instructions; }
    public List<Transition> transitions() { return transitions; }
    /** Actual final executed instruction, independent of the supplied list order. */
    public int lastAddress() { return lastAddress; }
    /** Actual resulting program counter after the block. */
    public int nextAddress() { return nextAddress; }

    /** An explicitly observed transition; neither endpoint is required to be a fall-through address. */
    public static final class Transition {
        private final int from;
        private final int to;

        public Transition(int from, int to) {
            this.from = from;
            this.to = to;
        }

        public int from() { return from; }
        public int to() { return to; }

        @Override public boolean equals(Object value) {
            if (!(value instanceof Transition)) return false;
            Transition other = (Transition) value;
            return from == other.from && to == other.to;
        }

        @Override public int hashCode() { return 31 * from + to; }
    }
}
