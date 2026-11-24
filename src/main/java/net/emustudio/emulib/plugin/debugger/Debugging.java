/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugin.debugger;

import net.emustudio.emulib.plugin.cpu.DecodedInstruction;

import java.util.Optional;
import java.util.OptionalInt;

/** Optional machine-specific debugging, implemented directly by the CPU plugin root. */
public interface Debugging {
    /** Instruction observation, optional guest-access tracking, or full presentation capture. */
    enum Capture { INSTRUCTION, ACCESSES, FULL }

    /** Compiles a machine-specific expression; evaluation and inspection are read-only. */
    Expression compile(String source);

    /** Returns a plain-text reference for the expression syntax, names, and functions accepted by this machine. */
    String getExpressionsHelp();

    /** Installs one observer with the requested observation detail; closing its handle detaches it. */
    AutoCloseable observe(Observer observer, Capture capture);

    interface Expression {
        boolean test();
        ValueView display();
    }

    interface Observer {
        /** True requests an intentional stop before guest effects. */
        boolean beforeStep();
        /**
         * True requests an intentional stop after this truthful semantic step.
         * Instruction facts come from before execution; nextAddress is the actual resulting PC.
         * Failed steps and interrupt-only boundaries must not invent decoded instructions or transitions.
         */
        boolean afterStep(boolean completed, Optional<DecodedInstruction> instruction,
                          OptionalInt nextAddress, Optional<StepView> capture);
        /**
         * Whether instruction-only execution may report truthful block facts instead of individual step callbacks.
         * Opt-in requires implementing {@link #afterBlock(ExecutedBlock)} and retaining any stop policy at boundaries.
         * Access tracking and full presentation capture always retain individual steps.
         */
        default boolean canBatchInstructions() { return false; }
        /**
         * Receives an authorized graph contribution, never a rich step capture or an ordered event log.
         * Previously reported, unchanged facts may be omitted. New observers must receive all facts they need;
         * every contribution includes the actual final instruction and its resulting PC transition.
         */
        default void afterBlock(ExecutedBlock block) {
            throw new UnsupportedOperationException("Instruction batching is not supported");
        }
        /** Reports isolated instrumentation failures; never requests a CPU stop. */
        default void failed(String message) { }
    }
}
