/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.plugins.debugger;

import net.emustudio.emulib.plugins.cpu.DecodedInstruction;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Serialized observer mechanics only. Machine interpretation and captures belong to the plugin. */
public final class DebuggingSupport {
    private static final Logger LOGGER = Logger.getLogger(DebuggingSupport.class.getName());
    private final Runnable beforeInstruction;
    private final Runnable beforeCapture;
    private final Supplier<StepView> afterCapture;
    private final Consumer<Boolean> activityChanged;
    private Debugging.Observer observer;
    private Debugging.Capture capture = Debugging.Capture.INSTRUCTION;
    private boolean stepping, instructionCaptured;
    private long observation;

    public DebuggingSupport(Runnable beforeInstruction, Runnable beforeCapture,
                            Supplier<StepView> afterCapture, Consumer<Boolean> activityChanged) {
        this.beforeInstruction = Objects.requireNonNull(beforeInstruction);
        this.beforeCapture = Objects.requireNonNull(beforeCapture);
        this.afterCapture = Objects.requireNonNull(afterCapture);
        this.activityChanged = Objects.requireNonNull(activityChanged);
    }

    public boolean isActive() { return observer != null; }
    public boolean isCapturing() { return observer != null && capture == Debugging.Capture.FULL; }
    public boolean requiresAccesses() { return observer != null && capture != Debugging.Capture.INSTRUCTION; }

    public AutoCloseable observe(Debugging.Observer next, Debugging.Capture capture) {
        Objects.requireNonNull(next);
        Objects.requireNonNull(capture);
        if (observer != null) throw new IllegalStateException("Debugging observer already installed");
        long installed = ++observation;
        observer = next;
        this.capture = capture;
        activity(requiresAccesses());
        return () -> {
            if (observation == installed && observer == next) detach();
        };
    }

    /** Called for every semantic step, including the inactive fast path. */
    public boolean begin() {
        stepping = false;
        instructionCaptured = false;
        if (observer == null) return true;
        try {
            if (observer.beforeStep()) return false;
        } catch (RuntimeException failure) {
            failed("Before-step observer failed", failure);
            detach();
            return true;
        }
        stepping = true;
        try {
            beforeInstruction.run();
            instructionCaptured = true;
        } catch (RuntimeException failure) {
            failed("Instruction capture failed", failure);
        }
        if (isCapturing()) {
            try { beforeCapture.run(); }
            catch (RuntimeException failure) { captureFailed(failure); }
        }
        return true;
    }

    public boolean end(boolean completed, Optional<DecodedInstruction> instruction, OptionalInt nextAddress) {
        if (!stepping || observer == null) return false;
        stepping = false;
        Optional<StepView> view = Optional.empty();
        if (isCapturing()) {
            try { view = Optional.of(Objects.requireNonNull(afterCapture.get())); }
            catch (RuntimeException failure) { captureFailed(failure); }
        }
        try {
            return observer.afterStep(completed, instructionCaptured ? instruction : Optional.empty(), nextAddress, view);
        } catch (RuntimeException failure) {
            failed("After-step observer failed", failure);
            detach();
            return false;
        }
    }

    private void captureFailed(RuntimeException failure) {
        // Preserve instruction observation and access watches after presentation capture fails.
        capture = Debugging.Capture.ACCESSES;
        failed("Step capture failed", failure);
    }

    private void detach() {
        observer = null;
        capture = Debugging.Capture.INSTRUCTION;
        activity(false);
    }

    private void activity(boolean active) {
        try { activityChanged.accept(active); }
        catch (RuntimeException failure) { failed("Debugging instrumentation failed", failure); }
    }

    private void failed(String description, RuntimeException failure) {
        String message = description + ": " + failure.getMessage();
        LOGGER.log(Level.WARNING, message, failure);
        if (observer != null) {
            try { observer.failed(message); }
            catch (RuntimeException reportingFailure) {
                LOGGER.log(Level.WARNING, "Debugging failure reporting failed", reportingFailure);
            }
        }
    }
}
