/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugin.debugger;

import net.emustudio.emulib.plugin.cpu.DecodedInstruction;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Observer handoff between settings and execution threads. Machine interpretation belongs to the plugin. */
public final class DebuggingSupport {
    private static final Logger LOGGER = Logger.getLogger(DebuggingSupport.class.getName());
    private final Runnable beforeInstruction;
    private final Runnable beforeCapture;
    private final Supplier<StepView> afterCapture;
    private final Consumer<Boolean> activityChanged;
    private final AtomicReference<Registration> registration = new AtomicReference<>();
    private final AtomicInteger activityRequests = new AtomicInteger();
    // Confined to the execution thread; rich captures belong to one registration's complete step.
    private Registration step;
    private Debugging.Capture stepCapture;
    private boolean instructionCaptured, fullCapturePrepared, batchInstructions;

    private static final class Registration {
        final Debugging.Observer observer;
        final CompletableFuture<Void> ready = new CompletableFuture<>();
        volatile Debugging.Capture capture;

        Registration(Debugging.Observer observer, Debugging.Capture capture) {
            this.observer = observer;
            this.capture = capture;
        }
    }

    public DebuggingSupport(Runnable beforeInstruction, Runnable beforeCapture,
                            Supplier<StepView> afterCapture, Consumer<Boolean> activityChanged) {
        this.beforeInstruction = Objects.requireNonNull(beforeInstruction);
        this.beforeCapture = Objects.requireNonNull(beforeCapture);
        this.afterCapture = Objects.requireNonNull(afterCapture);
        this.activityChanged = Objects.requireNonNull(activityChanged);
    }

    public boolean isActive() { return registration.get() != null; }
    public boolean isCapturing() {
        Registration current = registration.get();
        return current != null && current.capture == Debugging.Capture.FULL;
    }
    public boolean requiresAccesses() {
        Registration current = registration.get();
        return current != null && current.capture != Debugging.Capture.INSTRUCTION;
    }

    public AutoCloseable observe(Debugging.Observer next, Debugging.Capture capture) {
        Objects.requireNonNull(next);
        Objects.requireNonNull(capture);
        Registration installed = new Registration(next, capture);
        if (!registration.compareAndSet(null, installed))
            throw new IllegalStateException("Debugging observer already installed");
        activity();
        return () -> detach(installed);
    }

    /** Called for every semantic step, including the inactive fast path. */
    public boolean begin() {
        step = null;
        instructionCaptured = false;
        fullCapturePrepared = false;
        batchInstructions = false;
        Registration current = readyRegistration(null);
        if (current == null) return true;
        try {
            if (current.observer.beforeStep()) return false;
        } catch (RuntimeException failure) {
            failed(current, "Before-step observer failed", failure);
            detach(current);
            return true;
        }
        // A capture-mode update inside beforeStep still belongs to this observer's next instruction.
        current = readyRegistration(current.observer);
        if (current == null) return true;
        step = current;
        stepCapture = current.capture;
        try {
            beforeInstruction.run();
            instructionCaptured = true;
        } catch (RuntimeException failure) {
            failed(current, "Instruction capture failed", failure);
        }
        if (stepCapture == Debugging.Capture.FULL && registration.get() == current) {
            try { beforeCapture.run(); fullCapturePrepared = true; }
            catch (RuntimeException failure) { captureFailed(current, failure); }
        }
        return true;
    }

    public boolean end(boolean completed, Optional<DecodedInstruction> instruction, OptionalInt nextAddress) {
        Registration current = step;
        step = null;
        batchInstructions = false;
        if (!sameObserver(current, registration.get())) return false;
        Optional<StepView> view = Optional.empty();
        if (stepCapture == Debugging.Capture.FULL && fullCapturePrepared && registration.get() == current) {
            try { view = Optional.of(Objects.requireNonNull(afterCapture.get())); }
            catch (RuntimeException failure) { captureFailed(current, failure); }
        }
        Registration latest = registration.get();
        if (!sameObserver(current, latest)) return false;
        if (latest != current) view = Optional.empty();
        try {
            return current.observer.afterStep(completed, instructionCaptured ? instruction : Optional.empty(), nextAddress, view);
        } catch (RuntimeException failure) {
            failed(current, "After-step observer failed", failure);
            detach(current);
            return false;
        }
    }

    /** Queries opt-in after begin and before guest effects; a successful result authorizes this step's block. */
    public boolean canBatchInstructions() {
        batchInstructions = false;
        Registration current = step;
        Registration latest = registration.get();
        if (!instructionCaptured || stepCapture != Debugging.Capture.INSTRUCTION
                || !sameObserver(current, latest) || latest.capture != Debugging.Capture.INSTRUCTION) return false;
        try {
            boolean supported = current.observer.canBatchInstructions();
            latest = registration.get();
            batchInstructions = supported && step == current && sameObserver(current, latest)
                    && latest.capture == Debugging.Capture.INSTRUCTION;
        } catch (RuntimeException failure) {
            failed(current, "Instruction batching query failed", failure);
        }
        return batchInstructions;
    }

    /** Completes a block previously authorized by canBatchInstructions; mode changes cannot erase executed facts. */
    public void endBlock(ExecutedBlock block) {
        Registration current = step;
        step = null;
        boolean authorized = batchInstructions;
        batchInstructions = false;
        if (!sameObserver(current, registration.get())) return;
        if (!authorized) {
            failed(current, "Instruction block was not authorized", new IllegalStateException("Batching requires observer opt-in"));
            return;
        }
        try {
            current.observer.afterBlock(Objects.requireNonNull(block));
        } catch (RuntimeException failure) {
            failed(current, "After-block observer failed", failure);
            detach(current);
        }
    }

    private Registration readyRegistration(Debugging.Observer expected) {
        Registration current;
        do {
            current = registration.get();
            if (current == null || expected != null && current.observer != expected) return null;
            // Activity callbacks run without locks; wait before entering any observer callback.
            current.ready.join();
        } while (registration.get() != current);
        return current;
    }

    private static boolean sameObserver(Registration first, Registration second) {
        return first != null && second != null && first.observer == second.observer;
    }

    private void captureFailed(Registration current, RuntimeException failure) {
        // Preserve instruction observation and access watches after presentation capture fails.
        current.capture = Debugging.Capture.ACCESSES;
        stepCapture = Debugging.Capture.ACCESSES;
        failed(current, "Step capture failed", failure);
    }

    private void detach(Registration current) {
        if (!registration.compareAndSet(current, null)) return;
        current.ready.complete(null);
        activity();
    }

    private void activity() {
        // Drain reentrant/concurrent updates without holding a lock across plugin/observer callbacks.
        if (activityRequests.getAndIncrement() != 0) return;
        int pending = 1;
        do {
            Registration current = registration.get();
            try { activityChanged.accept(current != null && current.capture != Debugging.Capture.INSTRUCTION); }
            catch (RuntimeException failure) { failed(current, "Debugging instrumentation failed", failure); }
            finally { if (current != null) current.ready.complete(null); }
            pending = activityRequests.addAndGet(-pending);
        } while (pending != 0);
    }

    private void failed(Registration current, String description, RuntimeException failure) {
        String message = description + ": " + failure.getMessage();
        LOGGER.log(Level.WARNING, message, failure);
        if (current != null && registration.get() == current) {
            try { current.observer.failed(message); }
            catch (RuntimeException reportingFailure) {
                LOGGER.log(Level.WARNING, "Debugging failure reporting failed", reportingFailure);
            }
        }
    }
}
