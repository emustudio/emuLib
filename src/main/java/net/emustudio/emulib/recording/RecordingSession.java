/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.recording;

import net.jcip.annotations.ThreadSafe;
import org.jcodec.api.awt.AWTSequenceEncoder;
import org.jcodec.common.AudioFormat;
import org.jcodec.common.DemuxerTrackMeta;
import org.jcodec.common.io.FileChannelWrapper;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.Rational;
import org.jcodec.containers.mp4.MP4Packet;
import org.jcodec.containers.mp4.demuxer.MP4Demuxer;
import org.jcodec.containers.mp4.demuxer.MP4DemuxerTrack;
import org.jcodec.containers.mp4.muxer.CodecMP4MuxerTrack;
import org.jcodec.containers.mp4.muxer.MP4Muxer;
import org.jcodec.containers.mp4.muxer.PCMMP4MuxerTrack;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Thread-safe, one-time MP4 recording of video (H.264) and audio (PCM).
 *
 * <p>Video frames and audio arrive independently. Inputs are copied before returning, so producers may reuse their
 * buffers. Video is encoded on a dedicated worker into a temporary file; audio is stored compressed in another
 * temporary file and muxed in on {@link #stop(Path)}. Capture never waits for the worker: exceeding the bounded
 * queue fails the session instead of slowing emulation. Check {@link #getFailure()} when capture returns false.
 *
 * <p>Audio is signed 16-bit little-endian stereo PCM at the configured sample rate. Producers own frame pacing,
 * synthesis and mixing.
 *
 * <p>{@link #stop(Path)} and {@link #close()} wait for the worker and file I/O. Call them off the UI thread.
 */
@ThreadSafe
public final class RecordingSession implements AutoCloseable {
    static final String TEMP_FILE_PREFIX = "emustudio-recording-";
    private static final int QUEUE_CAPACITY = 256;
    private static final int AUDIO_FRAME_SIZE = 2 * Short.BYTES;
    private static final int COMPRESSION_BUFFER_SIZE = 64 * 1024;
    private static final Task STOP = () -> { };

    interface Task {
        void run() throws IOException;
    }

    final BlockingQueue<Task> queue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
    final Path videoFile;
    final Path audioFile;
    private final FileChannelWrapper videoChannel;
    private final AWTSequenceEncoder encoder;
    private final OutputStream audioOutput;
    private final Thread worker;
    private final Object stopLock = new Object();
    private final int width;
    private final int height;
    private final int audioSampleRate;
    private long videoFrames; // owned by worker
    private long audioBytes; // owned by worker
    private boolean stopped; // guarded by this
    private volatile IOException failure;

    /**
     * Starts a recording. Dimensions must be positive and even for H.264.
     *
     * @param width           video width in pixels
     * @param height          video height in pixels
     * @param videoScale      frame-rate denominator
     * @param videoRate       frame-rate numerator; fps = videoRate / videoScale
     * @param audioSampleRate stereo PCM frames per second
     * @throws IOException              if temporary recording files cannot be created
     * @throws IllegalArgumentException if dimensions, frame rate or sample rate are invalid
     */
    public RecordingSession(int width, int height, int videoScale, int videoRate, int audioSampleRate) throws IOException {
        if (width <= 0 || height <= 0 || (width & 1) != 0 || (height & 1) != 0) {
            throw new IllegalArgumentException("Recording dimensions must be positive and even");
        }
        if (videoScale <= 0 || videoRate <= 0 || audioSampleRate <= 0) {
            throw new IllegalArgumentException("Video timing and audio sample rate must be > 0");
        }
        this.width = width;
        this.height = height;
        this.audioSampleRate = audioSampleRate;
        this.videoFile = Files.createTempFile(TEMP_FILE_PREFIX, ".mp4");
        Path audio = null;
        FileChannelWrapper channel = null;
        try {
            channel = NIOUtils.writableChannel(videoFile.toFile());
            this.encoder = new AWTSequenceEncoder(channel, Rational.R(videoRate, videoScale));
            audio = Files.createTempFile(TEMP_FILE_PREFIX, ".pcm.gz");
            this.audioOutput = new GZIPOutputStream(new BufferedOutputStream(Files.newOutputStream(audio)), COMPRESSION_BUFFER_SIZE);
        } catch (IOException | RuntimeException e) {
            NIOUtils.closeQuietly(channel);
            Files.deleteIfExists(videoFile);
            if (audio != null) Files.deleteIfExists(audio);
            throw e;
        }
        this.audioFile = audio;
        this.videoChannel = channel;
        worker = new Thread(this::drainQueue, "emuStudio-recording");
        worker.setDaemon(true);
        worker.start();
    }

    /**
     * Captures one video frame.
     *
     * @param frame video frame of the recording size; null frames are ignored
     * @return false if the frame was ignored, or the session is stopped or failed
     */
    public synchronized boolean captureVideo(BufferedImage frame) {
        if (!canCapture() || frame == null) return false;
        if (frame.getWidth() != width || frame.getHeight() != height) {
            return fail(new IOException("Recording frame size changed during capture"));
        }
        BufferedImage copy = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = copy.createGraphics();
        try {
            graphics.drawImage(frame, 0, 0, null);
        } finally {
            graphics.dispose();
        }
        return enqueue(() -> {
            encoder.encodeImage(copy);
            videoFrames++;
        });
    }

    /**
     * Captures the first length bytes of PCM audio. Empty chunks are ignored.
     *
     * @param pcmSamples PCM buffer, which may be reused after this call
     * @param length     number of bytes, containing only whole stereo PCM frames
     * @return false if the session is stopped or failed
     * @throws NullPointerException      if an active producer passes a null buffer
     * @throws IndexOutOfBoundsException if length is outside the buffer
     */
    public synchronized boolean captureAudio(byte[] pcmSamples, int length) {
        if (!canCapture()) return false;
        Objects.checkFromIndexSize(0, length, Objects.requireNonNull(pcmSamples).length);
        if (length % AUDIO_FRAME_SIZE != 0) {
            return fail(new IOException("Recording audio contains an incomplete PCM frame"));
        }
        if (length == 0) return true;
        byte[] copy = Arrays.copyOf(pcmSamples, length);
        return enqueue(() -> {
            audioOutput.write(copy);
            audioBytes += copy.length;
        });
    }

    /**
     * @return the capture failure, or null while healthy. Safe to poll from producer/UI threads.
     */
    public IOException getFailure() {
        return failure;
    }

    /**
     * Stops accepting capture and saves MP4 to target, or discards the recording when target is null.
     * Saving drains accepted captures; discarding drops pending captures. Repeated calls do nothing.
     * Temporary files are always deleted; an existing target is kept if export fails.
     *
     * @param target destination MP4 (parent directories are created), or null to discard
     * @throws IOException if capture or export failed, the recording has no frames, or stopping was interrupted
     */
    public void stop(Path target) throws IOException {
        synchronized (stopLock) {
            synchronized (this) {
                if (stopped) return;
                stopped = true;
                if (target == null || failure != null) queue.clear();
            }
            try {
                queue.put(STOP);
                worker.join();
            } catch (InterruptedException e) {
                queue.clear();
                worker.interrupt();
                joinUninterruptibly(worker);
                closeFiles(false);
                deleteQuietly(videoFile, audioFile);
                Thread.currentThread().interrupt();
                throw failure = new IOException("Interrupted while stopping recording", e);
            }
            try {
                closeFiles(target != null);
                if (target != null) {
                    if (failure != null) throw failure;
                    export(target);
                }
            } finally {
                deleteQuietly(videoFile, audioFile);
            }
        }
    }

    /**
     * Discards this recording and releases its files. Blocks until the worker stops.
     */
    @Override
    public void close() throws IOException {
        stop(null);
    }

    private boolean canCapture() {
        return !stopped && failure == null;
    }

    private boolean fail(IOException e) {
        failure = e;
        return false;
    }

    private boolean enqueue(Task task) {
        return queue.offer(task) || fail(new IOException("Recording encoder could not keep up with capture"));
    }

    private void drainQueue() {
        try {
            Task task;
            while ((task = queue.take()) != STOP) {
                if (failure == null) {
                    try {
                        task.run();
                    } catch (IOException | RuntimeException e) {
                        failure = new IOException("Could not record video or audio", e);
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void closeFiles(boolean finish) {
        try {
            if (finish && failure == null && videoFrames > 0) encoder.finish();
            videoChannel.close();
            audioOutput.close();
        } catch (IOException | RuntimeException e) {
            failure = new IOException("Could not finalize recording", e);
            NIOUtils.closeQuietly(videoChannel);
            NIOUtils.closeQuietly(audioOutput);
        }
    }

    private void export(Path target) throws IOException {
        if (videoFrames == 0) throw new IOException("Recording contains no video frames");
        Path output = Files.createTempFile(TEMP_FILE_PREFIX, ".mp4");
        try {
            if (audioBytes == 0) {
                Files.copy(videoFile, output, StandardCopyOption.REPLACE_EXISTING);
            } else {
                muxAudio(output);
            }
            if (target.getParent() != null) Files.createDirectories(target.getParent());
            try {
                Files.move(output, target, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                Files.copy(output, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(output);
        }
    }

    private void muxAudio(Path output) throws IOException {
        try (FileChannelWrapper input = NIOUtils.readableChannel(videoFile.toFile());
             MP4Demuxer demuxer = MP4Demuxer.createMP4Demuxer(input);
             FileChannelWrapper outputChannel = NIOUtils.writableChannel(output.toFile());
             InputStream audio = new GZIPInputStream(new BufferedInputStream(Files.newInputStream(audioFile)), COMPRESSION_BUFFER_SIZE)) {
            MP4DemuxerTrack sourceTrack = (MP4DemuxerTrack) demuxer.getVideoTrack();
            DemuxerTrackMeta meta = (sourceTrack == null) ? null : sourceTrack.getMeta();
            if (meta == null || meta.getCodec() == null) throw new IOException("Encoded recording has no usable video track");

            MP4Muxer muxer = MP4Muxer.createMP4MuxerToChannel(outputChannel);
            CodecMP4MuxerTrack videoTrack = (CodecMP4MuxerTrack) muxer.addVideoTrack(meta.getCodec(), meta.getVideoCodecMeta());
            MP4Packet packet;
            while ((packet = sourceTrack.nextFrame()) != null) {
                videoTrack.addFrame(packet);
            }
            PCMMP4MuxerTrack audioTrack = muxer.addPCMAudioTrack(new AudioFormat(audioSampleRate, Short.SIZE, 2, true, false));
            // chunks are whole PCM frames, because the buffer size is a multiple of the frame size
            byte[] chunk = new byte[AUDIO_FRAME_SIZE * 4096];
            int read;
            while ((read = audio.readNBytes(chunk, 0, chunk.length)) > 0) {
                audioTrack.addSamples(ByteBuffer.wrap(Arrays.copyOf(chunk, read)));
            }
            muxer.finish();
        }
    }

    private static void joinUninterruptibly(Thread thread) {
        while (true) {
            try {
                thread.join();
                return;
            } catch (InterruptedException ignored) {
                // the caller's interrupt is restored after cleanup
            }
        }
    }

    private static void deleteQuietly(Path... files) {
        for (Path file : files) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException ignored) {
                // best effort; temporary directory is cleaned by the system
            }
        }
    }
}
