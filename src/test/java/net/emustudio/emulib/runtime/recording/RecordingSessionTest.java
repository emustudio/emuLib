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
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class RecordingSessionTest {
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void exportsVideoAndAudioWithMatchingDurations() throws Exception {
        Path output = folder.getRoot().toPath().resolve("nested/dir/capture.mp4");
        byte[] audio = new byte[(48000 / 60) * 4];
        ByteBuffer.wrap(audio).order(ByteOrder.LITTLE_ENDIAN).putShort((short) 2000).putShort((short) 2000);
        RecordingSession session = new RecordingSession(32, 32, 1, 60, 48000);
        assertFalse(session.captureVideo(null));
        assertTrue(session.captureAudio(new byte[0], 0));
        for (int i = 0; i < 60; i++) {
            assertTrue(session.captureVideo(frame(Color.GREEN)));
            assertTrue(session.captureAudio(audio, audio.length));
        }
        session.stop(output);
        assertTempFilesDeleted(session);
        assertFalse(session.captureVideo(frame(Color.RED)));
        assertFalse(session.captureAudio(audio, audio.length));
        session.stop(null);
        session.close();
        try (var channel = NIOUtils.readableChannel(output.toFile());
             var demuxer = MP4Demuxer.createMP4Demuxer(channel)) {
            var video = demuxer.getVideoTrack().getMeta();
            var sound = demuxer.getAudioTracks().get(0).getMeta();
            assertEquals(1, demuxer.getAudioTracks().size());
            assertEquals(60, video.getTotalFrames());
            assertEquals(1.0, video.getTotalDuration(), 0.001);
            assertEquals(1.0, sound.getTotalDuration(), 0.001);
            assertEquals(48000, sound.getAudioCodecMeta().getSampleRate());
            assertEquals(2, sound.getAudioCodecMeta().getChannelCount());
            assertEquals(32, video.getVideoCodecMeta().getSize().getWidth());
        }
    }

    @Test
    public void preservesFractionalVideoRateAndStereoSamples() throws Exception {
        Path output = folder.getRoot().toPath().resolve("fractional.mp4");
        int scale = 69_888;
        int rate = 3_500_000;
        byte[] audio = new byte[44100 * 4];
        ByteBuffer.wrap(audio).order(ByteOrder.LITTLE_ENDIAN).putShort((short) 1234).putShort((short) -5678);
        try (RecordingSession session = new RecordingSession(32, 32, scale, rate, 44100)) {
            for (int i = 0; i < 50; i++) {
                assertTrue(session.captureVideo(frame(Color.BLUE)));
            }
            assertTrue(session.captureAudio(audio, audio.length));
            session.stop(output);
        }
        try (var channel = NIOUtils.readableChannel(output.toFile());
             var demuxer = MP4Demuxer.createMP4Demuxer(channel)) {
            assertEquals(50, demuxer.getVideoTrack().getMeta().getTotalFrames());
            assertEquals(50.0 * scale / rate, demuxer.getVideoTrack().getMeta().getTotalDuration(), 0.000001);
            var sound = demuxer.getAudioTracks().get(0);
            assertEquals(44100, sound.getMeta().getAudioCodecMeta().getSampleRate());
            assertEquals(1.0, sound.getMeta().getTotalDuration(), 0.000001);
            ByteBuffer samples = sound.nextFrame().getData().order(ByteOrder.LITTLE_ENDIAN);
            assertEquals(1234, samples.getShort());
            assertEquals(-5678, samples.getShort());
        }
    }

    @Test
    public void videoWithoutAudioHasNoAudioTrack() throws Exception {
        Path output = folder.getRoot().toPath().resolve("silent.mp4");
        try (RecordingSession session = new RecordingSession(32, 32, 1, 50, 48000)) {
            for (int i = 0; i < 5; i++) assertTrue(session.captureVideo(frame(Color.GREEN)));
            session.stop(output);
        }
        try (var channel = NIOUtils.readableChannel(output.toFile());
             var demuxer = MP4Demuxer.createMP4Demuxer(channel)) {
            assertEquals(5, demuxer.getVideoTrack().getMeta().getTotalFrames());
            assertTrue(demuxer.getAudioTracks().isEmpty());
        }
    }

    @Test
    public void copiesProducerBuffersBeforeReturning() throws Exception {
        Path output = folder.getRoot().toPath().resolve("copied.mp4");
        BufferedImage image = frame(Color.BLUE);
        byte[] audio = new byte[3200];
        ByteBuffer.wrap(audio).order(ByteOrder.LITTLE_ENDIAN).putShort((short) 1234).putShort((short) -5678);
        try (RecordingSession session = new RecordingSession(32, 32, 1, 60, 48000)) {
            CountDownLatch release = blockWorker(session);
            try {
                assertTrue(session.captureVideo(image));
                assertTrue(session.captureAudio(audio, audio.length));
                Arrays.fill(audio, (byte) 0);
                image.setRGB(16, 16, Color.RED.getRGB());
            } finally {
                release.countDown();
            }
            session.stop(output);
        }
        Color pixel = new Color(AWTFrameGrab.getFrame(output.toFile(), 0).getRGB(16, 16));
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
    public void invalidCaptureFailsRecordingWithoutCreatingOutput() throws Exception {
        Path output = folder.getRoot().toPath().resolve("invalid.mp4");
        for (int variant = 0; variant < 2; variant++) {
            RecordingSession session = new RecordingSession(32, 32, 1, 60, 48000);
            assertTrue(session.captureVideo(frame(Color.GREEN)));
            if (variant == 0) {
                assertFalse(session.captureAudio(new byte[3], 3));
            } else {
                assertFalse(session.captureVideo(new BufferedImage(34, 32, BufferedImage.TYPE_INT_RGB)));
            }
            assertNotNull(session.getFailure());
            assertFalse(session.captureVideo(frame(Color.GREEN)));
            assertThrows(IOException.class, () -> session.stop(output));
            assertFalse(Files.exists(output));
            assertTempFilesDeleted(session);
        }
    }

    @Test
    public void rejectsInvalidArguments() throws Exception {
        for (int[] args : new int[][]{{31, 32, 1, 60, 48000}, {0, 32, 1, 60, 48000}, {32, -1, 1, 60, 48000},
                {32, 32, 0, 60, 48000}, {32, 32, 1, -1, 48000}, {32, 32, 1, 60, 0}}) {
            assertThrows(IllegalArgumentException.class, () -> new RecordingSession(args[0], args[1], args[2], args[3], args[4]));
        }
        try (RecordingSession session = new RecordingSession(32, 32, 1, 60, 48000)) {
            assertThrows(IndexOutOfBoundsException.class, () -> session.captureAudio(new byte[4], 8));
            assertThrows(NullPointerException.class, () -> session.captureAudio(null, 4));
            assertNull(session.getFailure());
        }
    }

    @Test
    public void discardOrEmptyRecordingDeletesTemporaryFiles() throws Exception {
        RecordingSession discarded = new RecordingSession(32, 32, 1, 60, 48000);
        assertTrue(discarded.captureVideo(frame(Color.GREEN)));
        assertTrue(discarded.captureAudio(new byte[3200], 3200));
        discarded.stop(null);
        assertTempFilesDeleted(discarded);

        RecordingSession empty = new RecordingSession(32, 32, 1, 60, 48000);
        Path output = folder.getRoot().toPath().resolve("empty.mp4");
        assertThrows(IOException.class, () -> empty.stop(output));
        assertFalse(Files.exists(output));
        assertTempFilesDeleted(empty);
    }

    @Test
    public void fullQueueRejectsFramesWithoutBlockingAndFailsRecording() throws Exception {
        RecordingSession session = new RecordingSession(32, 32, 1, 60, 48000);
        CountDownLatch release = blockWorker(session);
        Path output = folder.getRoot().toPath().resolve("overloaded.mp4");
        try {
            int capacity = session.queue.remainingCapacity();
            for (int i = 0; i < capacity; i++) {
                assertTrue(session.captureVideo(frame(Color.GREEN)));
            }
            assertFalse(session.captureVideo(frame(Color.GREEN)));
            assertNotNull(session.getFailure());
            release.countDown();
            assertThrows(IOException.class, () -> session.stop(output));
            assertFalse(Files.exists(output));
        } finally {
            release.countDown();
            session.close();
        }
    }

    @Test
    public void interruptedStopDiscardsFilesAndPreservesCallerInterrupt() throws Exception {
        RecordingSession session = new RecordingSession(32, 32, 1, 60, 48000);
        Thread.currentThread().interrupt();
        try {
            assertThrows(IOException.class, () -> session.stop(folder.getRoot().toPath().resolve("interrupted.mp4")));
            assertTrue(Thread.currentThread().isInterrupted());
            assertTempFilesDeleted(session);
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
            assertTrue(session.captureVideo(frame(Color.BLUE)));
            assertTrue(session.captureAudio(new byte[3200], 3200));
            assertThrows(IOException.class, () -> session.stop(destination));
            assertTempFilesDeleted(session);
            assertEquals("keep", Files.readString(existing));
        }
    }

    private static CountDownLatch blockWorker(RecordingSession session) throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        session.queue.put(() -> {
            entered.countDown();
            try {
                release.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new UncheckedIOException(new IOException(e));
            }
        });
        assertTrue(entered.await(5, TimeUnit.SECONDS));
        return release;
    }

    private static void assertTempFilesDeleted(RecordingSession session) {
        assertFalse(Files.exists(session.videoFile));
        assertFalse(Files.exists(session.audioFile));
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
