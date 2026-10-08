/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.recording;

import org.jcodec.api.awt.AWTFrameGrab;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.containers.mp4.demuxer.MP4Demuxer;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class IndependentCaptureTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void preservesFractionalVideoRateAndIndependentStereoAudioAt44100Hz() throws Exception {
        Path output = folder.getRoot().toPath().resolve("fractional.mp4");
        int scale = 69_888;
        int rate = 3_500_000;
        byte[] audio = new byte[44100 * 4];
        ByteBuffer.wrap(audio).order(ByteOrder.LITTLE_ENDIAN).putShort((short) 1234).putShort((short) -5678);
        try (RecordingSession session = new RecordingSession(32, 32, scale, rate, 44100)) {
            assertFalse(session.captureVideo(null));
            assertTrue(session.captureAudio(new byte[0], 0));
            for (int i = 0; i < 50; i++) {
                assertTrue(session.captureVideo(frame(Color.BLUE)));
            }
            assertTrue(session.captureAudio(audio, audio.length));
            session.stop(output);
            assertFalse(session.captureVideo(frame(Color.RED)));
        }
        try (var channel = NIOUtils.readableChannel(output.toFile());
             var demuxer = MP4Demuxer.createMP4Demuxer(channel)) {
            assertEquals(50, demuxer.getVideoTrack().getMeta().getTotalFrames());
            assertEquals(50.0 * scale / rate, demuxer.getVideoTrack().getMeta().getTotalDuration(), 0.000001);
            var sound = demuxer.getAudioTracks().get(0);
            assertEquals(44100, sound.getMeta().getAudioCodecMeta().getSampleRate());
            assertEquals(2, sound.getMeta().getAudioCodecMeta().getChannelCount());
            assertEquals(1.0, sound.getMeta().getTotalDuration(), 0.000001);
            ByteBuffer samples = sound.nextFrame().getData().order(ByteOrder.LITTLE_ENDIAN);
            assertEquals(1234, samples.getShort());
            assertEquals(-5678, samples.getShort());
        }
    }

    @Test
    public void copiesBothProducerBuffersBeforeReturning() throws Exception {
        Path output = folder.getRoot().toPath().resolve("copied.mp4");
        BufferedImage image = frame(Color.BLUE);
        byte[] audio = new byte[3200];
        ByteBuffer.wrap(audio).order(ByteOrder.LITTLE_ENDIAN).putShort((short) 1234).putShort((short) -5678);
        try (RecordingSession session = new RecordingSession(32, 32, 1, 60, 48000)) {
            CountDownLatch entered = new CountDownLatch(1);
            CountDownLatch release = blockWorker(session, entered);
            try {
                assertTrue(entered.await(5, TimeUnit.SECONDS));
                assertTrue(session.captureVideo(image));
                assertTrue(session.captureAudio(audio, audio.length));
                Arrays.fill(audio, (byte) 0);
                image.setRGB(16, 16, Color.RED.getRGB());
            } finally {
                release.countDown();
            }
            session.stop(output);
        }
        BufferedImage recorded = AWTFrameGrab.getFrame(output.toFile(), 0);
        Color pixel = new Color(recorded.getRGB(16, 16));
        assertTrue(pixel.getBlue() > 200);
        assertTrue(pixel.getRed() < 50);
        try (var channel = NIOUtils.readableChannel(output.toFile());
             var demuxer = MP4Demuxer.createMP4Demuxer(channel)) {
            ByteBuffer samples = demuxer.getAudioTracks().get(0).nextFrame().getData().order(ByteOrder.LITTLE_ENDIAN);
            assertEquals(1234, samples.getShort());
            assertEquals(-5678, samples.getShort());
        }
    }

    @Test
    public void misalignedAudioAndWrongSizeFailWithoutCreatingOutput() throws Exception {
        Path output = folder.getRoot().toPath().resolve("invalid.mp4");
        try (RecordingSession session = new RecordingSession(32, 32, 1, 60, 48000)) {
            assertFalse(session.captureAudio(new byte[3], 3));
            assertNotNull(session.getFailure());
            assertThrows(IOException.class, () -> session.stop(output));
        }
        assertFalse(Files.exists(output));
        try (RecordingSession session = new RecordingSession(32, 32, 1, 60, 48000)) {
            assertFalse(session.captureVideo(new BufferedImage(34, 32, BufferedImage.TYPE_INT_RGB)));
            assertNotNull(session.getFailure());
            assertThrows(IOException.class, () -> session.stop(output));
        }
        assertFalse(Files.exists(output));
    }

    @Test
    public void rejectsOddDimensionsAndInvalidAudioLength() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> new RecordingSession(31, 32, 1, 60, 48000));
        try (RecordingSession session = new RecordingSession(32, 32, 1, 60, 48000)) {
            assertThrows(IndexOutOfBoundsException.class, () -> session.captureAudio(new byte[4], 8));
            assertThrows(NullPointerException.class, () -> session.captureAudio(null, 4));
        }
    }

    @Test
    public void interruptedStopDiscardsFilesAndPreservesCallerInterrupt() throws Exception {
        RecordingSession session = new RecordingSession(32, 32, 1, 60, 48000);
        Field recorderField = RecordingSession.class.getDeclaredField("recorder");
        recorderField.setAccessible(true);
        Object recorder = recorderField.get(session);
        Field fileField = VideoRecorder.class.getDeclaredField("tempVideoFile");
        fileField.setAccessible(true);
        Path video = (Path) fileField.get(recorder);
        Thread.currentThread().interrupt();
        try {
            assertThrows(IOException.class, () -> session.stop(folder.getRoot().toPath().resolve("interrupted.mp4")));
            assertTrue(Thread.currentThread().isInterrupted());
            assertFalse(Files.exists(video));
        } finally {
            Thread.interrupted();
            session.close();
        }
    }

    @Test
    public void failedExportReleasesCaptureFilesAndPreservesExistingDestination() throws Exception {
        Path destination = folder.newFolder("occupied").toPath();
        Path existing = destination.resolve("keep.txt");
        Files.writeString(existing, "keep");
        try (RecordingSession session = new RecordingSession(32, 32, 1, 60, 48000)) {
            Field field = RecordingSession.class.getDeclaredField("recorder");
            field.setAccessible(true);
            Object recorder = field.get(session);
            Field videoField = VideoRecorder.class.getDeclaredField("tempVideoFile");
            Field audioField = VideoRecorder.class.getDeclaredField("tempAudioFile");
            videoField.setAccessible(true);
            audioField.setAccessible(true);
            Path video = (Path) videoField.get(recorder);
            Path audio = (Path) audioField.get(recorder);
            assertTrue(session.capture(frame(Color.BLUE), new byte[3200]));
            assertThrows(IOException.class, () -> session.stop(destination));
            assertFalse(Files.exists(video));
            assertFalse(Files.exists(audio));
            assertEquals("keep", Files.readString(existing));
        }
    }

    @SuppressWarnings("unchecked")
    private static CountDownLatch blockWorker(RecordingSession session, CountDownLatch entered) throws Exception {
        Field field = RecordingSession.class.getDeclaredField("queue");
        field.setAccessible(true);
        BlockingQueue<Runnable> queue = (BlockingQueue<Runnable>) field.get(session);
        CountDownLatch release = new CountDownLatch(1);
        queue.put(() -> {
            entered.countDown();
            try {
                release.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        return release;
    }

    private static BufferedImage frame(Color color) {
        BufferedImage frame = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
        var graphics = frame.createGraphics();
        graphics.setColor(color);
        graphics.fillRect(0, 0, 32, 32);
        graphics.dispose();
        return frame;
    }
}
