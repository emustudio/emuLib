/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.recording;

import net.jcip.annotations.ThreadSafe;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Thread-safe MP4 video and audio recording, with H.264 encoding on a dedicated worker.
 *
 * <p>Video and audio may arrive independently, or together through {@link #capture(BufferedImage, byte[])}.
 * Inputs are copied before returning, so producers may reuse their buffers. Capture never waits for the worker;
 * exceeding the bounded queue fails the session instead of slowing emulation or retaining unlimited frames.
 * Check {@link #getFailure()} when capture returns false.
 *
 * <p>Audio is signed 16-bit little-endian stereo PCM at the configured sample rate. Producers own frame pacing,
 * synthesis and mixing. The session does not change audio samples or round the rational video frame rate.
 *
 * <p>{@link #stop(Path)} and {@link #close()} wait for the worker and file I/O. Call them off the UI thread.
 */
@ThreadSafe
public final class RecordingSession implements AutoCloseable {
    private static final int QUEUE_CAPACITY = 256;
    private static final int AUDIO_FRAME_SIZE = 2 * Short.BYTES;
    private static final Runnable STOP = () -> { };
    private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
    private final VideoRecorder recorder;
    private final Thread worker;
    private final Object stopLock = new Object();
    private final int width;
    private final int height;
    private boolean accepting = true;
    private boolean stopped;
    private volatile IOException failure;

    /**
     * Creates one recording. Dimensions must be positive and even for H.264.
     *
     * @param width video width in pixels
     * @param height video height in pixels
     * @param videoScale frame-rate denominator
     * @param videoRate frame-rate numerator; fps = videoRate / videoScale
     * @param audioSampleRate stereo PCM frames per second
     * @throws IOException if temporary recording files cannot be created
     * @throws IllegalArgumentException if dimensions, frame rate or sample rate are invalid
     */
    public RecordingSession(int width, int height, int videoScale, int videoRate, int audioSampleRate) throws IOException {
        this.width = width;
        this.height = height;
        recorder = new VideoRecorder(width, height, videoScale, videoRate, audioSampleRate);
        worker = new Thread(this::drainQueue, "emuStudio-recording");
        worker.setDaemon(true);
        worker.start();
    }

    /** Captures one video frame. Null frames are ignored. Returns false if ignored, stopped or failed. */
    public synchronized boolean captureVideo(BufferedImage frame) {
        if (!canCapture() || frame == null) {
            return false;
        }
        if (!validFrame(frame)) {
            return false;
        }
        BufferedImage copy = copyFrame(frame);
        return enqueue(() -> recorder.captureVideo(copy));
    }

    /**
     * Captures the first length bytes of PCM. Empty chunks are ignored; returns false if stopped or failed.
     * @param pcmSamples PCM buffer, which may be reused after this call
     * @param length number of bytes, containing only whole stereo PCM frames
     * @return whether capture was accepted
     * @throws NullPointerException if an active producer passes a null buffer
     * @throws IndexOutOfBoundsException if length is outside the buffer
     */
    public synchronized boolean captureAudio(byte[] pcmSamples, int length) {
        if (!canCapture()) {
            return false;
        }
        if (!validAudio(pcmSamples, length)) {
            return false;
        }
        if (length == 0) {
            return true;
        }
        byte[] copy = Arrays.copyOf(pcmSamples, length);
        return enqueue(() -> recorder.captureAudio(copy));
    }

    /** Captures a video frame and its PCM together, in one queue entry. Both inputs are copied. */
    public synchronized boolean capture(BufferedImage frame, byte[] pcmSamples) {
        if (!canCapture() || frame == null) {
            return false;
        }
        Objects.requireNonNull(pcmSamples);
        if (!validFrame(frame) || !validAudio(pcmSamples, pcmSamples.length)) {
            return false;
        }
        BufferedImage image = copyFrame(frame);
        byte[] audio = Arrays.copyOf(pcmSamples, pcmSamples.length);
        return enqueue(() -> {
            recorder.captureVideo(image);
            recorder.captureAudio(audio);
        });
    }

    /** Returns the capture failure, or null while healthy. Safe to poll from producer/UI threads. */
    public IOException getFailure() {
        return failure;
    }

    /**
     * Stops accepting capture and saves MP4 to target, or discards it when target is null.
     * Saving drains accepted captures; discarding drops pending captures. Repeated calls do nothing.
     * All temporary files are released, including after failed capture or export.
     * @param target destination MP4, or null to discard
     * @throws IOException if capture/export failed or stopping was interrupted
     */
    public void stop(Path target) throws IOException {
        synchronized (stopLock) {
            synchronized (this) {
                if (stopped) {
                    return;
                }
                stopped = true;
                accepting = false;
                if (target == null || failure != null) {
                    queue.clear();
                }
            }
            try {
                queue.put(STOP);
                worker.join();
            } catch (InterruptedException e) {
                failure = new IOException("Interrupted while stopping recording", e);
                queue.clear();
                worker.interrupt();
                // Wait for ownership of recorder to return before closing its files.
                boolean waiting = true;
                while (waiting) {
                    try {
                        worker.join();
                        waiting = false;
                    } catch (InterruptedException ignored) {
                        // Restore the interrupt after cleanup.
                    }
                }
                recorder.stop(null);
                Thread.currentThread().interrupt();
                throw failure;
            }
            if (failure != null) {
                recorder.stop(null);
                if (target != null) {
                    throw failure;
                }
            } else {
                recorder.stop(target);
            }
        }
    }

    /** Discards this recording and releases its files. Blocks until the worker stops. */
    @Override
    public void close() throws IOException {
        stop(null);
    }

    private boolean canCapture() {
        return accepting && failure == null;
    }

    private boolean validFrame(BufferedImage frame) {
        if (frame.getWidth() != width || frame.getHeight() != height) {
            failure = new IOException("Recording frame size changed during capture");
            return false;
        }
        return true;
    }

    private boolean validAudio(byte[] pcm, int length) {
        Objects.requireNonNull(pcm);
        Objects.checkFromIndexSize(0, length, pcm.length);
        if (length % AUDIO_FRAME_SIZE != 0) {
            failure = new IOException("Recording audio contains an incomplete PCM frame");
            return false;
        }
        return true;
    }

    private static BufferedImage copyFrame(BufferedImage frame) {
        BufferedImage copy = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = copy.createGraphics();
        try {
            graphics.drawImage(frame, 0, 0, null);
        } finally {
            graphics.dispose();
        }
        return copy;
    }

    private boolean enqueue(Runnable capture) {
        if (!canCapture()) {
            return false;
        }
        if (!queue.offer(capture)) {
            failure = new IOException("Recording encoder could not keep up with capture");
            return false;
        }
        return true;
    }

    private void drainQueue() {
        try {
            Runnable task;
            while ((task = queue.take()) != STOP) {
                if (failure == null) {
                    task.run();
                    IOException captureFailure = recorder.getCaptureFailure();
                    if (captureFailure != null) {
                        failure = captureFailure;
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
