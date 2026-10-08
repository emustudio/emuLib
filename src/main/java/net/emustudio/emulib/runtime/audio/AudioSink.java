/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.audio;

import javax.sound.sampled.LineUnavailableException;

/**
 * Receives interleaved, signed 16-bit little-endian PCM frames.
 * Producers may reuse the input buffer after {@link #accept(byte[], int)} returns.
 */
@FunctionalInterface
public interface AudioSink extends AutoCloseable {
    /** Discards all audio, for headless execution or unavailable audio devices. */
    AudioSink NULL = (samples, length) -> { };

    /**
     * Opens a Java Sound output with a bounded queue and a dedicated writer thread.
     * Playback does not block the producer; a full queue drops the newest batch.
     *
     * @param sampleRate frames per second
     * @param channels number of interleaved channels
     * @return an open sink owned by the caller
     * @throws IllegalArgumentException if the format is invalid or unsupported
     * @throws LineUnavailableException if no audio device can be opened
     */
    static AudioSink open(int sampleRate, int channels) throws LineUnavailableException {
        return JavaSoundAudioSink.open(sampleRate, channels);
    }

    /**
     * Accepts {@code length} valid bytes from the start of the buffer.
     * The bytes must contain complete PCM frames. Writes after close are discarded.
     *
     * @param pcmSamples PCM buffer
     * @param length number of valid bytes
     */
    void accept(byte[] pcmSamples, int length);

    /** Discards queued audio and flushes the host line, rather than waiting for playback. */
    default void flushAudio() { }

    /** Releases output resources. Closing an output repeatedly is safe. */
    @Override
    default void close() { }
}
