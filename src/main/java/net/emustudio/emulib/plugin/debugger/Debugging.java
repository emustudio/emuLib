/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugin.debugger;

import java.util.Optional;

/** Optional machine-specific debugging, implemented directly by the CPU plugin root. */
public interface Debugging {
    /**
     * Installs the host's live-edit callback before execution starts. Invoke it on the CPU worker before
     * every outer semantic step, including when no debugging observer is active.
     */
    void onBoundary(Runnable callback);

    /** Compile/evaluate through the existing execution service or observer callbacks; inspection is read-only. */
    Expression compile(String source);

    /** Install one observer and close its handle through the existing execution service. */
    AutoCloseable observe(Observer observer, boolean capture);

    interface Expression {
        boolean test();
        ValueView display();
    }

    interface Observer {
        /** True requests an intentional stop before guest effects. */
        boolean beforeStep();
        /** True requests an intentional stop after this truthful semantic step. */
        boolean afterStep(boolean completed, Optional<StepView> capture);
        /** Reports isolated instrumentation failures; never requests a CPU stop. */
        default void failed(String message) { }
    }
}
