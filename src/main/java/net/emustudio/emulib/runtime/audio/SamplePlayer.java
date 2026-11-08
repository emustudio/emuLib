/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.audio;

import javax.sound.sampled.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Loads and plays independent samples identified by caller-defined keys.
 * Files are converted to signed 16-bit little-endian mono PCM at the chosen sample rate.
 * Samples can overlap, restart or loop; playback calls do not wait for effects to finish.
 *
 * @param <K> sample identifier, such as a plugin's own enum
 */
public final class SamplePlayer<K> implements AutoCloseable {
    private final AudioFormat format;
    private final ClipFactory clipFactory;
    private final Map<K, Sample> samples = new HashMap<>();
    private int volumePercent = 100;

    /**
     * Creates an empty player, initially at full volume. No device is opened until a sample is loaded.
     *
     * @param sampleRate playback and captured-audio frames per second
     */
    public SamplePlayer(int sampleRate) {
        this(sampleRate, AudioSystem::getClip);
    }

    SamplePlayer(int sampleRate, ClipFactory clipFactory) {
        if (sampleRate <= 0) {
            throw new IllegalArgumentException("Sample rate must be positive");
        }
        format = new AudioFormat(sampleRate, 16, 1, true, false);
        this.clipFactory = Objects.requireNonNull(clipFactory);
    }

    /**
     * Loads or replaces a sample. A failed load keeps any previously loaded sample.
     * Callers decide how to report missing files or unavailable audio devices.
     *
     * @param key  sample identifier
     * @param path audio file
     * @throws IOException                   if the file cannot be read
     * @throws UnsupportedAudioFileException if the file is not a supported audio format
     * @throws LineUnavailableException      if the audio device cannot open the sample
     * @throws IllegalArgumentException      if conversion to the playback format is unsupported
     */
    public synchronized void load(K key, Path path)
            throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        Objects.requireNonNull(key);
        try (AudioInputStream stream = AudioSystem.getAudioInputStream(path.toFile());
             AudioInputStream pcm = AudioSystem.getAudioInputStream(format, stream)) {
            byte[] data = pcm.readAllBytes();
            short[] sampleData = new short[data.length / Short.BYTES];
            ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(sampleData);
            Clip clip = clipFactory.create();
            try {
                clip.open(format, data, 0, data.length);
                put(key, clip, sampleData);
            } catch (LineUnavailableException | RuntimeException | Error e) {
                clip.close();
                throw e;
            }
        }
    }

    // Installs an opened clip, replacing (and closing) a previous one
    synchronized void put(K key, Clip clip, short[] data) {
        applyVolume(clip);
        Sample previous = samples.put(key, new Sample(clip, data));
        if (previous != null) {
            previous.clip.stop();
            previous.clip.close();
        }
    }

    /**
     * Restarts the identified sample, optionally looping it until stopped.
     * Unknown sample identifiers are silent.
     *
     * @param key  sample identifier
     * @param loop whether to repeat continuously
     */
    public synchronized void play(K key, boolean loop) {
        Sample sample = samples.get(key);
        if (sample != null) {
            sample.clip.stop();
            sample.clip.setFramePosition(0);
            sample.looping = loop;
            if (loop) {
                sample.clip.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                sample.clip.start();
            }
        }
    }

    /**
     * Stops one sample; unknown identifiers are ignored.
     *
     * @param key sample identifier
     */
    public synchronized void stop(K key) {
        Sample sample = samples.get(key);
        if (sample != null) {
            sample.looping = false;
            sample.clip.stop();
        }
    }

    /**
     * Stops all samples without unloading them.
     */
    public synchronized void stopAll() {
        samples.keySet().forEach(this::stop);
    }

    /**
     * @return the current volume, from 0 to 100
     */
    public synchronized int getVolumePercent() {
        return volumePercent;
    }

    /**
     * Sets the volume for loaded and future samples, clamped to 0..100.
     *
     * @param volumePercent volume in percent
     */
    public synchronized void setVolumePercent(int volumePercent) {
        this.volumePercent = Math.max(0, Math.min(100, volumePercent));
        samples.values().forEach(sample -> applyVolume(sample.clip));
    }

    private void applyVolume(Clip clip) {
        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float decibels = volumePercent == 0 ? gain.getMinimum()
                    : (float) (20 * Math.log10(volumePercent / 100.0));
            gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), decibels)));
        }
        if (clip.isControlSupported(BooleanControl.Type.MUTE)) {
            BooleanControl mute = (BooleanControl) clip.getControl(BooleanControl.Type.MUTE);
            mute.setValue(volumePercent == 0);
        }
    }

    /**
     * Mixes samples at their current playback positions for recording.
     * Returns signed 16-bit little-endian stereo PCM with identical left/right channels,
     * applying the current volume and saturating overlapping samples. Missing or stopped samples
     * contribute silence. This reads playback positions; it does not advance them.
     *
     * @param frameCount number of stereo frames to capture
     * @return an independent PCM buffer
     */
    public synchronized byte[] captureAudio(int frameCount) {
        if (frameCount < 0 || frameCount > Integer.MAX_VALUE / (2 * Short.BYTES)) {
            throw new IllegalArgumentException("Invalid audio frame count");
        }
        long[] mixed = new long[frameCount];
        for (Sample sample : samples.values()) {
            short[] data = sample.data;
            if (data.length == 0 || !sample.clip.isRunning()) {
                continue;
            }
            long position = sample.clip.getLongFramePosition();
            for (int frame = 0; frame < frameCount; frame++) {
                long index = position + frame;
                if (sample.looping || index < data.length) {
                    mixed[frame] += data[(int) (index % data.length)];
                }
            }
        }
        ByteBuffer pcm = ByteBuffer.allocate(frameCount * 2 * Short.BYTES).order(ByteOrder.LITTLE_ENDIAN);
        for (long value : mixed) {
            short sample = (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, value * volumePercent / 100));
            pcm.putShort(sample).putShort(sample);
        }
        return pcm.array();
    }

    /**
     * Releases all loaded samples. Repeated calls are safe; volume is retained and new samples
     * may be loaded afterwards.
     */
    @Override
    public synchronized void close() {
        samples.values().forEach(sample -> {
            sample.clip.stop();
            sample.clip.close();
        });
        samples.clear();
    }

    @FunctionalInterface
    interface ClipFactory {
        Clip create() throws LineUnavailableException;
    }

    private static final class Sample {
        final Clip clip;
        final short[] data;
        boolean looping;

        Sample(Clip clip, short[] data) {
            this.clip = clip;
            this.data = data;
        }
    }
}
