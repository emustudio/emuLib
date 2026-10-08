/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

final class JavaSoundAudioSink implements AudioSink {
    private static final System.Logger LOGGER = System.getLogger(JavaSoundAudioSink.class.getName());
    private static final int QUEUE_CAPACITY = 32;
    private static final int BATCH_FRAMES = 512;
    private final SourceDataLine line;
    private final int frameSize;
    private final BlockingQueue<byte[]> queue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    private final Thread worker;
    private volatile boolean accepting = true;

    static AudioSink open(int sampleRate, int channels) throws LineUnavailableException {
        if (sampleRate <= 0 || channels <= 0) {
            throw new IllegalArgumentException("Sample rate and channels must be positive");
        }
        AudioFormat format = new AudioFormat(sampleRate, 16, channels, true, false);
        return new JavaSoundAudioSink(AudioSystem.getSourceDataLine(format), format);
    }

    JavaSoundAudioSink(SourceDataLine line, AudioFormat format) throws LineUnavailableException {
        this.line = line;
        this.frameSize = format.getFrameSize();
        worker = new Thread(this::drainQueue, "emuLib-audio-output");
        worker.setDaemon(true);
        try {
            int bufferSize = Math.max(BATCH_FRAMES * 16, (int) format.getSampleRate() / 2) * frameSize;
            line.open(format, bufferSize);
            line.start();
            // Signed PCM silence is zero, independent of the producer's hardware waveform.
            line.write(new byte[bufferSize], 0, bufferSize);
            worker.start();
        } catch (LineUnavailableException | RuntimeException | Error e) {
            line.close();
            throw e;
        }
    }

    @Override
    public void accept(byte[] pcmSamples, int length) {
        if (length < 0 || length > pcmSamples.length || length % frameSize != 0) {
            throw new IllegalArgumentException("Audio batch must contain complete PCM frames");
        }
        synchronized (queue) {
            if (accepting && length > 0) {
                queue.offer(Arrays.copyOf(pcmSamples, length));
            }
        }
    }

    @Override
    public void flushAudio() {
        synchronized (queue) {
            if (!accepting) {
                return;
            }
            queue.clear();
        }
        line.flush();
    }

    @Override
    public void close() {
        synchronized (queue) {
            if (!accepting) {
                return;
            }
            accepting = false;
            queue.clear();
        }
        worker.interrupt();
        // Closing the line releases a writer blocked inside SourceDataLine.write().
        try {
            line.stop();
            line.flush();
        } finally {
            line.close();
        }
        if (Thread.currentThread() != worker) {
            try {
                worker.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void drainQueue() {
        try {
            while (accepting) {
                byte[] chunk = queue.poll(10, TimeUnit.MILLISECONDS);
                if (chunk == null) {
                    continue;
                }
                int offset = 0;
                while (accepting && offset < chunk.length) {
                    int written = line.write(chunk, offset, chunk.length - offset);
                    if (written <= 0) {
                        break;
                    }
                    offset += written;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (RuntimeException e) {
            if (accepting) {
                LOGGER.log(System.Logger.Level.WARNING, "Audio output stopped", e);
            }
        } finally {
            close();
        }
    }
}
