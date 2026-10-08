/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.audio;

import org.junit.Test;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class JavaSoundAudioSinkTest {
    private static final AudioFormat FORMAT = new AudioFormat(48000, 16, 2, true, false);

    @FunctionalInterface
    private interface Write {
        int accept(byte[] bytes, int offset, int length) throws InterruptedException;
    }

    private SourceDataLine line(Write write, Runnable onClose) throws LineUnavailableException {
        SourceDataLine line = createMock(SourceDataLine.class);
        makeThreadSafe(line, false);
        line.open(same(FORMAT), anyInt());
        AtomicBoolean priming = new AtomicBoolean(true);
        expect(line.write(anyObject(byte[].class), anyInt(), anyInt())).andAnswer(() -> {
            byte[] bytes = getCurrentArgument(0);
            int offset = getCurrentArgument(1);
            int length = getCurrentArgument(2);
            if (priming.getAndSet(false)) {
                assertArrayEquals(new byte[length], bytes);
                return length;
            }
            return write.accept(bytes, offset, length);
        }).anyTimes();
        line.start();
        line.stop();
        line.flush();
        expectLastCall().anyTimes();
        line.close();
        expectLastCall().andAnswer(() -> {
            onClose.run();
            return null;
        });
        replay(line);
        return line;
    }

    private static void await(CountDownLatch latch) throws InterruptedException {
        assertTrue("Audio worker did not respond", latch.await(2, TimeUnit.SECONDS));
    }

    @Test(timeout = 5000)
    public void copiesReusableBuffersAndCompletesPartialWrites() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(1);
        List<byte[]> writes = Collections.synchronizedList(new ArrayList<>());
        SourceDataLine line = line((bytes, offset, length) -> {
            entered.countDown();
            await(release);
            writes.add(Arrays.copyOfRange(bytes, offset, offset + 4));
            if (offset == 4) {
                done.countDown();
            }
            return 4;
        }, release::countDown);
        try (AudioSink sink = new JavaSoundAudioSink(line, FORMAT)) {
            byte[] input = {1, 2, 3, 4, 5, 6, 7, 8};
            sink.accept(input, input.length);
            await(entered);
            Arrays.fill(input, (byte) 0);
            release.countDown();
            await(done);
            assertEquals(2, writes.size());
            assertArrayEquals(new byte[]{1, 2, 3, 4}, writes.get(0));
            assertArrayEquals(new byte[]{5, 6, 7, 8}, writes.get(1));
        }
        verify(line);
    }

    @Test(timeout = 5000)
    public void fullQueueDropsNewestBatchesWithoutBlockingProducer() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(33);
        List<Byte> writes = Collections.synchronizedList(new ArrayList<>());
        SourceDataLine line = line((bytes, offset, length) -> {
            entered.countDown();
            await(release);
            writes.add(bytes[0]);
            done.countDown();
            return length;
        }, release::countDown);
        try (AudioSink sink = new JavaSoundAudioSink(line, FORMAT)) {
            sink.accept(new byte[4], 4);
            await(entered);
            for (int i = 1; i <= 40; i++) {
                sink.accept(new byte[]{(byte) i, 0, 0, 0}, 4);
            }
            release.countDown();
            await(done);
            assertEquals(33, writes.size());
            for (int i = 0; i <= 32; i++) {
                assertEquals((byte) i, writes.get(i).byteValue());
            }
        }
        verify(line);
    }

    @Test(timeout = 5000)
    public void flushDiscardsPendingBatchesAndAcceptsNewAudio() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(2);
        List<Byte> writes = Collections.synchronizedList(new ArrayList<>());
        SourceDataLine line = line((bytes, offset, length) -> {
            entered.countDown();
            await(release);
            writes.add(bytes[0]);
            done.countDown();
            return length;
        }, release::countDown);
        try (AudioSink sink = new JavaSoundAudioSink(line, FORMAT)) {
            sink.accept(new byte[]{1, 0, 0, 0}, 4);
            await(entered);
            sink.accept(new byte[]{2, 0, 0, 0}, 4);
            sink.flushAudio();
            sink.accept(new byte[]{3, 0, 0, 0}, 4);
            release.countDown();
            await(done);
            assertEquals(List.of((byte) 1, (byte) 3), writes);
        }
        verify(line);
    }

    @Test(timeout = 5000)
    public void closeReleasesBlockedWriterOnceAndDiscardsSubsequentWrites() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch closed = new CountDownLatch(1);
        CountDownLatch exited = new CountDownLatch(1);
        SourceDataLine line = line((bytes, offset, length) -> {
            entered.countDown();
            // The host write may ignore interruption; closing the line must release it.
            boolean interrupted = false;
            while (closed.getCount() != 0) {
                try {
                    closed.await();
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
            exited.countDown();
            return 0;
        }, closed::countDown);
        AudioSink sink = new JavaSoundAudioSink(line, FORMAT);
        try {
            sink.accept(new byte[4], 4);
            await(entered);
        } finally {
            sink.close();
        }
        await(exited);
        sink.close();
        sink.flushAudio();
        sink.accept(new byte[4], 4);
        verify(line);
    }

    @Test
    public void openFailureClosesAcquiredLine() throws Exception {
        SourceDataLine line = createMock(SourceDataLine.class);
        line.open(same(FORMAT), anyInt());
        expectLastCall().andThrow(new LineUnavailableException("Unavailable"));
        line.close();
        replay(line);
        assertThrows(LineUnavailableException.class, () -> new JavaSoundAudioSink(line, FORMAT));
        verify(line);
    }

    @Test
    public void rejectsInvalidFormatAndIncompleteFrames() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> AudioSink.open(0, 2));
        assertThrows(IllegalArgumentException.class, () -> AudioSink.open(48000, 0));
        SourceDataLine line = line((bytes, offset, length) -> length, () -> { });
        try (AudioSink sink = new JavaSoundAudioSink(line, FORMAT)) {
            assertThrows(IllegalArgumentException.class, () -> sink.accept(new byte[4], 3));
            assertThrows(IllegalArgumentException.class, () -> sink.accept(new byte[4], 8));
            assertThrows(IllegalArgumentException.class, () -> sink.accept(new byte[4], -1));
        }
        verify(line);
    }
}
