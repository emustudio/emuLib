/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.debugger;

import net.emustudio.emulib.plugins.cpu.DecodedInstruction;

import java.util.Optional;
import java.util.OptionalInt;

/** Optional machine-specific debugging, implemented directly by the CPU plugin root. */
public interface Debugging {
    /** Instruction observation, optional guest-access tracking, or full presentation capture. */
    enum Capture { INSTRUCTION, ACCESSES, FULL }

    /** Compile/evaluate through the existing execution service or observer callbacks; inspection is read-only. */
    Expression compile(String source);

    /** Install one observer and close its handle through the existing execution service. */
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
        /** Reports isolated instrumentation failures; never requests a CPU stop. */
        default void failed(String message) { }
    }
}
