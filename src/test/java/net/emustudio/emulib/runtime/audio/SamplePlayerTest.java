/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.audio;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.util.Map;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class SamplePlayerTest {
    @Rule
    public TemporaryFolder temporary = new TemporaryFolder();
    private static final String SHOT = "shot";
    private static final String UFO = "loop";
    private static final String BONUS = "missing";
    private static final String PLAYER_HIT = "hit";

    private static SamplePlayer<String> player() {
        return player(Map.of());
    }

    private static SamplePlayer<String> player(Map<String, Clip> clips) {
        return player(clips, Map.of());
    }

    private static SamplePlayer<String> player(Map<String, Clip> clips, Map<String, short[]> samples) {
        SamplePlayer<String> player = new SamplePlayer<>(48000, clips, samples);
        return player;
    }

    @Test
    public void loadsWavAndCapturesDecodedSamplesAsStereo() throws Exception {
        Clip clip = createStrictMock(Clip.class);
        clip.open(anyObject(AudioFormat.class), anyObject(byte[].class), eq(0), eq(6));
        noVolumeControls(clip);
        clip.stop();
        clip.setFramePosition(0);
        clip.start();
        expect(clip.isRunning()).andReturn(true);
        expect(clip.getLongFramePosition()).andReturn(1L);
        clip.stop();
        clip.close();
        replay(clip);
        try (SamplePlayer<String> output = new SamplePlayer<>(48000, () -> clip)) {
            assertEquals(100, output.getVolumePercent());
            output.load(SHOT, wav(new short[]{1000, 2000, -3000}));
            output.play(SHOT, false);
            assertArrayEquals(new short[]{2000, 2000, -3000, -3000, 0, 0}, pcm(output.captureAudio(3)));
        }
        verify(clip);
    }

    @Test
    public void failedReplacementClosesNewClipAndKeepsPreviousSample() throws Exception {
        Clip original = createStrictMock(Clip.class);
        original.open(anyObject(AudioFormat.class), anyObject(byte[].class), eq(0), eq(2));
        noVolumeControls(original);
        original.stop();
        original.setFramePosition(0);
        original.start();
        original.stop();
        original.close();
        Clip failed = createStrictMock(Clip.class);
        failed.open(anyObject(AudioFormat.class), anyObject(byte[].class), eq(0), eq(2));
        expectLastCall().andThrow(new LineUnavailableException("Unavailable"));
        failed.close();
        replay(original, failed);
        java.util.Iterator<Clip> clips = List.of(original, failed).iterator();
        Path file = wav(new short[]{1000});
        try (SamplePlayer<String> output = new SamplePlayer<>(48000, clips::next)) {
            output.load(SHOT, file);
            assertThrows(LineUnavailableException.class, () -> output.load(SHOT, file));
            output.play(SHOT, false);
        }
        verify(original, failed);
    }

    @Test
    public void missingFileFailsBeforeOpeningAudioDevice() {
        SamplePlayer<String> output = new SamplePlayer<>(48000, () -> {
            throw new AssertionError("Missing file must not open an audio device");
        });
        assertThrows(IOException.class, () -> output.load(SHOT, temporary.getRoot().toPath().resolve("missing.wav")));
        assertArrayEquals(new byte[8], output.captureAudio(2));
    }

    @Test
    public void rejectsInvalidRatesAndCaptureLengths() {
        assertThrows(IllegalArgumentException.class, () -> new SamplePlayer<>(0));
        SamplePlayer<String> output = new SamplePlayer<>(48000);
        assertThrows(IllegalArgumentException.class, () -> output.captureAudio(-1));
        assertThrows(IllegalArgumentException.class, () -> output.captureAudio(Integer.MAX_VALUE));
    }

    private Path wav(short[] samples) throws IOException {
        ByteBuffer bytes = ByteBuffer.allocate(samples.length * Short.BYTES).order(ByteOrder.LITTLE_ENDIAN);
        bytes.asShortBuffer().put(samples);
        Path file = temporary.newFile().toPath();
        try (AudioInputStream stream = new AudioInputStream(new ByteArrayInputStream(bytes.array()),
                new AudioFormat(48000, 16, 1, true, false), samples.length)) {
            AudioSystem.write(stream, AudioFileFormat.Type.WAVE, file.toFile());
        }
        return file;
    }

    @Test
    public void restartsEffectsAndLoopsUfoWithoutWaiting() {
        Clip shot = createStrictMock(Clip.class);
        noVolumeControls(shot);
        shot.stop();
        shot.setFramePosition(0);
        shot.start();
        Clip ufo = createStrictMock(Clip.class);
        noVolumeControls(ufo);
        ufo.stop();
        ufo.setFramePosition(0);
        ufo.loop(Clip.LOOP_CONTINUOUSLY);
        ufo.stop();
        replay(shot, ufo);
        SamplePlayer<String> output = player(Map.of(SHOT, shot, UFO, ufo));
        output.play(SHOT, false);
        output.play(UFO, true);
        output.stop(UFO);
        verify(shot, ufo);
    }

    @Test
    public void missingSampleIsSilentAndCloseReleasesEveryClipOnce() {
        Clip clip = createStrictMock(Clip.class);
        noVolumeControls(clip);
        clip.stop();
        clip.stop();
        clip.close();
        replay(clip);
        SamplePlayer<String> output = player(Map.of(SHOT, clip));
        output.play(BONUS, false);
        output.stop(BONUS);
        output.stopAll();
        output.close();
        output.close();
        output.play(SHOT, false);
        verify(clip);
    }

    @Test
    public void volumeControlsAllClipsAndZeroMutes() {
        FloatControl shotGain = gainControl();
        FloatControl ufoGain = gainControl();
        BooleanControl shotMute = new BooleanControl(BooleanControl.Type.MUTE, false) { };
        BooleanControl ufoMute = new BooleanControl(BooleanControl.Type.MUTE, false) { };
        Clip shot = clipWithControls(shotGain, shotMute);
        Clip ufo = clipWithControls(ufoGain, ufoMute);
        SamplePlayer<String> output = player(Map.of(SHOT, shot, UFO, ufo));
        output.setVolumePercent(25);
        assertEquals(25, output.getVolumePercent());
        assertEquals(-12.0412f, shotGain.getValue(), 0.001f);
        assertEquals(shotGain.getValue(), ufoGain.getValue(), 0);
        output.setVolumePercent(0);
        assertTrue(shotMute.getValue());
        assertTrue(ufoMute.getValue());
        assertEquals(shotGain.getMinimum(), shotGain.getValue(), 0);
        output.setVolumePercent(50);
        assertFalse(shotMute.getValue());
        assertFalse(ufoMute.getValue());
        assertEquals(-6.0206f, shotGain.getValue(), 0.001f);
        assertEquals(shotGain.getValue(), ufoGain.getValue(), 0);
        verify(shot, ufo);
    }

    @Test
    public void volumeIsClampedAndRetainedWhenNoClipsAreLoaded() {
        SamplePlayer<String> output = player();
        output.setVolumePercent(150);
        assertEquals(100, output.getVolumePercent());
        output.setVolumePercent(-1);
        assertEquals(0, output.getVolumePercent());
        output.setVolumePercent(40);
        output.close();
        assertEquals(40, output.getVolumePercent());
    }

    @Test
    public void recordedAudioMixesEffectsAtCurrentPositionAndVolume() {
        Clip shot = playingClip(1);
        Clip hit = playingClip(0);
        SamplePlayer<String> output = player(Map.of(SHOT, shot, PLAYER_HIT, hit),
                Map.of(SHOT, new short[]{0, 2000, -2000}, PLAYER_HIT, new short[]{1000, 1000}));
        output.setVolumePercent(50);
        assertArrayEquals(new short[]{1500, 1500, -500, -500, 0, 0}, pcm(output.captureAudio(3)));
        output.setVolumePercent(0);
        assertArrayEquals(new short[6], pcm(output.captureAudio(3)));
    }

    @Test
    public void recordedAudioLoopsUfoAndClampsOverlappingEffects() {
        Clip ufo = playingClip(3);
        Clip shot = playingClip(0);
        SamplePlayer<String> output = player(Map.of(UFO, ufo, SHOT, shot),
                Map.of(UFO, new short[]{20000, -20000}, SHOT, new short[]{-20000, 20000}));
        output.setVolumePercent(100);
        output.play(UFO, true);
        assertArrayEquals(new short[]{Short.MIN_VALUE, Short.MIN_VALUE, Short.MAX_VALUE, Short.MAX_VALUE},
                pcm(output.captureAudio(2)));
    }

    @Test
    public void missingOrStoppedEffectsRecordSilence() {
        Clip stopped = createNiceMock(Clip.class);
        replay(stopped);
        SamplePlayer<String> output = player(Map.of(SHOT, stopped), Map.of(SHOT, new short[]{2000}));
        assertArrayEquals(new short[4], pcm(output.captureAudio(2)));
        assertArrayEquals(new short[4], pcm(player().captureAudio(2)));
    }

    private static Clip playingClip(long framePosition) {
        Clip clip = createNiceMock(Clip.class);
        expect(clip.isRunning()).andReturn(true).anyTimes();
        expect(clip.getLongFramePosition()).andReturn(framePosition).anyTimes();
        replay(clip);
        return clip;
    }

    private static short[] pcm(byte[] data) {
        short[] samples = new short[data.length / Short.BYTES];
        ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(samples);
        return samples;
    }

    private static FloatControl gainControl() {
        return new FloatControl(FloatControl.Type.MASTER_GAIN, -80, 6, 0.1f, -1, 0, "dB") { };
    }

    private static Clip clipWithControls(FloatControl gain, BooleanControl mute) {
        Clip clip = createMock(Clip.class);
        expect(clip.isControlSupported(FloatControl.Type.MASTER_GAIN)).andReturn(true).anyTimes();
        expect(clip.getControl(FloatControl.Type.MASTER_GAIN)).andReturn(gain).anyTimes();
        expect(clip.isControlSupported(BooleanControl.Type.MUTE)).andReturn(true).anyTimes();
        expect(clip.getControl(BooleanControl.Type.MUTE)).andReturn(mute).anyTimes();
        replay(clip);
        return clip;
    }

    private static void noVolumeControls(Clip clip) {
        expect(clip.isControlSupported(FloatControl.Type.MASTER_GAIN)).andReturn(false).anyTimes();
        expect(clip.isControlSupported(BooleanControl.Type.MUTE)).andReturn(false).anyTimes();
    }
}
