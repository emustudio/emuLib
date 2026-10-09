/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.io;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class FileLoaderTest {
    @Rule public TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void recognizesAliasesWithoutApplyingFallback() {
        for (String extension : List.of("bin", "com", "out", "rom", "BIN")) {
            assertEquals(FileLoader.Format.BIN, FileLoader.forPath(Path.of("x." + extension)).orElseThrow().getFormat());
        }
        assertEquals(FileLoader.Format.HEX, FileLoader.forPath(Path.of("x.HeX")).orElseThrow().getFormat());
        assertFalse(FileLoader.forPath(Path.of("x.unknown")).isPresent());
        assertFalse(FileLoader.forPath(Path.of("directory.tap/no-extension")).isPresent());
        assertFalse(FileLoader.Format.BIN.isTape());
        assertTrue(FileLoader.Format.TAP.isTape());
    }

    @Test
    public void binaryIsUnaddressedAndBlocksOwnTheirBytes() throws IOException {
        Path path = file("image.bin", new byte[]{1, 2, 3});
        List<FileLoader.Block> blocks = new ArrayList<>();
        FileLoader.forFormat(FileLoader.Format.BIN).load(path, blocks::add, FileLoader.Options.fileOrder());
        FileLoader.Block block = blocks.get(0);
        assertTrue(block.getAddress().isEmpty());
        byte[] bytes = block.getData();
        bytes[0] = 99;
        assertArrayEquals(new byte[]{1, 2, 3}, block.getData());
    }

    @Test
    public void hexKeepsSparseAddressesAndLastOverlappingRecord() throws IOException {
        Path path = tmp.newFile("sparse.hex").toPath();
        Files.writeString(path, ":020010001122BB\n:0100200033AC\n:0100110044AA\n:00000001FF\n");
        Map<Integer, Byte> bytes = new HashMap<>();
        FileLoader.forFormat(FileLoader.Format.HEX).load(path, block -> bytes.put(block.getAddress().orElseThrow(), block.getData()[0]), FileLoader.Options.fileOrder());
        assertEquals(Map.of(0x10, (byte) 0x11, 0x11, (byte) 0x44, 0x20, (byte) 0x33), bytes);
    }

    @Test
    public void spectrumHeadersExposeMetadataWithoutPlacingBytes() throws IOException {
        List<FileLoader.Block> blocks = new ArrayList<>();
        Path path = file("code.tap", tap(header(3, 0x1234, 3), packet(255, new byte[]{0x55, 0x66})));
        FileLoader.forFormat(FileLoader.Format.TAP).load(path, blocks::add, FileLoader.Options.fileOrder());
        FileLoader.SpectrumHeader header = blocks.get(0).getSpectrumHeader().orElseThrow();
        assertEquals(3, header.getType());
        assertEquals(0x1234, header.getParameter1());
        assertEquals(3, header.getDataLength());
        assertEquals(8, blocks.get(0).getUsedBitsInLastByte());
        assertTrue(blocks.get(0).getAddress().isEmpty());
        assertTrue(blocks.get(1).getSpectrumHeader().isEmpty());
        assertArrayEquals(packet(255, new byte[]{0x55, 0x66}), blocks.get(1).getData());
    }

    @Test
    public void corruptTapeBytesRemainAvailableAndPlayable() throws IOException {
        byte[] corrupt = packet(255, new byte[]{0x42});
        corrupt[corrupt.length - 1] ^= 1;
        Path path = file("corrupt.tap", tap(corrupt));
        FileLoader loader = FileLoader.forFormat(FileLoader.Format.TAP);
        TapeListener listener = createStrictMock(TapeListener.class);
        listener.onFileStart();
        listener.onDataStart();
        listener.onBlockFlag(255);
        listener.onBlockData(aryEq(new byte[]{0x42}));
        listener.onBlockChecksum(corrupt[corrupt.length - 1]);
        listener.onFileEnd();
        replay(listener);
        loader.load(path, new FileLoader.Listener() {
            @Override public void onFileStart() { listener.onFileStart(); }
            @Override public void onBlock(FileLoader.Block block) throws IOException { TapeReader.emit(block, listener); }
            @Override public void onFileEnd() { listener.onFileEnd(); }
        }, FileLoader.Options.playback());
        verify(listener);

        List<FileLoader.Block> blocks = new ArrayList<>();
        loader.load(path, blocks::add, FileLoader.Options.fileOrder());
        FileLoader.Block block = blocks.get(0);
        assertFalse(block.hasValidChecksum());
        byte[] body = block.getBytes();
        body[0] = 0;
        assertArrayEquals(corrupt, block.getBytes());
    }

    @Test
    public void tapeListenerWorksDirectlyWithGenericLoader() throws IOException {
        List<String> events = new ArrayList<>();
        TapeListener listener = new TapeListener() {
            @Override public void onFileStart() { events.add("start"); }
            @Override public void onHeaderStart() { }
            @Override public void onDataStart() { events.add("data"); }
            @Override public void onBlockFlag(int flag) { events.add("flag:" + flag); }
            @Override public void onBlockData(byte[] data) { events.add("bytes:" + data[0]); }
            @Override public void onBlockChecksum(byte checksum) { events.add("checksum:" + checksum); }
            @Override public void onFileEnd() { events.add("end"); }
            @Override public void onProgram(String name, int length, int autoStart, int programLength) { }
            @Override public void onNumberArray(String name, int length, char variable) { }
            @Override public void onStringArray(String name, int length, char variable) { }
            @Override public void onMemoryBlock(String name, int length, int address) { }
        };
        FileLoader.forFormat(FileLoader.Format.TAP).load(
                file("direct.tap", tap(packet(255, new byte[]{0x42}))), listener, FileLoader.Options.playback());
        assertEquals(List.of("start", "data", "flag:255", "bytes:66", "checksum:-67", "end"), events);
    }

    @Test
    public void malformedLengthsAndUnknownBlocksBecomeIoExceptions() throws IOException {
        for (byte[] content : List.of(new byte[]{1}, new byte[]{1, 0, 0}, new byte[]{5, 0, 0, 1})) {
            Path path = file("bad" + tmp.getRoot().list().length + ".tap", content);
            assertThrows(IOException.class, () -> FileLoader.forFormat(FileLoader.Format.TAP)
                    .load(path, block -> { }, FileLoader.Options.fileOrder()));
        }
        Path unknown = file("unknown.tzx", tzx(new byte[]{0x7F}));
        Path truncated = file("truncated.tzx", tzx(new byte[]{0x11, 0}));
        Path negative = file("negative.tzx", tzx(new byte[]{0x35, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, -1, -1, -1, -1}));
        for (Path path : List.of(unknown, truncated, negative)) {
            assertThrows(IOException.class, () -> FileLoader.forFormat(FileLoader.Format.TZX)
                    .load(path, block -> { }, FileLoader.Options.fileOrder()));
        }
    }

    @Test
    public void fileOrderAndPlaybackOrderHaveDistinctControlFlow() throws IOException {
        Path path = file("jump.tzx", tzx(new byte[]{0x23, 2, 0, 0x12, 1, 0, 1, 0, 0x12, 2, 0, 1, 0}));
        List<Integer> physical = new ArrayList<>();
        List<Integer> playback = new ArrayList<>();
        FileLoader loader = FileLoader.forFormat(FileLoader.Format.TZX);
        loader.load(path, block -> {
            assertTrue(block.getSpectrumHeader().isEmpty());
            physical.add(block.getId());
        }, FileLoader.Options.fileOrder());
        loader.load(path, block -> playback.add(block.getId()), FileLoader.Options.playback());
        assertEquals(List.of(0x23, 0x12, 0x12), physical);
        assertEquals(List.of(0x23, 0x12), playback);
    }

    @Test
    public void nestedLoopsAndCallSequencesTraverseWithoutLosingReturnAddress() throws IOException {
        Path loops = file("loops.tzx", tzx(new byte[]{0x24, 2, 0, 0x24, 3, 0, 0x12, 1, 0, 1, 0, 0x25, 0x25}));
        AtomicInteger tones = new AtomicInteger();
        FileLoader loader = FileLoader.forFormat(FileLoader.Format.TZX);
        loader.load(loops, b -> { if (b.getId() == 0x12) tones.incrementAndGet(); }, FileLoader.Options.playback());
        assertEquals(6, tones.get());

        // Call blocks 2 and 4, return after each; main path then jumps to EOF.
        Path calls = file("calls.tzx", tzx(new byte[]{0x26, 2, 0, 2, 0, 4, 0, 0x23, 5, 0,
                0x12, 1, 0, 1, 0, 0x27, 0x12, 2, 0, 1, 0, 0x27}));
        List<Integer> lengths = new ArrayList<>();
        loader.load(calls, b -> {
            FileLoader.Block block = b;
            if (block.getId() == 0x12) lengths.add(block.buffer().getShort() & 0xFFFF);
        }, FileLoader.Options.playback());
        assertEquals(List.of(1, 2), lengths);
    }

    @Test
    public void cancellationTerminatesBackwardJumpAndOmitsCompletion() throws IOException {
        Path path = file("loop.tzx", tzx(new byte[]{0x23, 0, 0}));
        AtomicInteger visits = new AtomicInteger();
        AtomicBoolean completed = new AtomicBoolean();
        FileLoader.Listener listener = new FileLoader.Listener() {
            @Override public void onBlock(FileLoader.Block block) { visits.incrementAndGet(); }
            @Override public void onFileEnd() { completed.set(true); }
        };
        assertThrows(InterruptedIOException.class, () -> FileLoader.forFormat(FileLoader.Format.TZX)
                .load(path, listener, FileLoader.Options.playback().withCancellation(() -> visits.get() >= 4)));
        assertEquals(4, visits.get());
        assertFalse(completed.get());
    }

    @Test
    public void interruptionAndListenerFailuresOmitCompletion() throws IOException {
        Path path = file("file.bin", new byte[]{1});
        AtomicBoolean completed = new AtomicBoolean();
        FileLoader.Listener listener = new FileLoader.Listener() {
            @Override public void onBlock(FileLoader.Block block) throws IOException { throw new IOException("Listener failed"); }
            @Override public void onFileEnd() { completed.set(true); }
        };
        FileLoader loader = FileLoader.forFormat(FileLoader.Format.BIN);
        assertThrows(IOException.class, () -> loader.load(path, listener, FileLoader.Options.fileOrder()));
        Thread.currentThread().interrupt();
        try {
            assertThrows(InterruptedIOException.class, () -> loader.load(path, listener, FileLoader.Options.fileOrder()));
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
        assertFalse(completed.get());
    }

    private Path file(String name, byte[] content) throws IOException {
        Path path = tmp.newFile(name).toPath();
        return Files.write(path, content);
    }

    private static byte[] packet(int flag, byte[] data) {
        byte[] packet = new byte[data.length + 2];
        packet[0] = (byte) flag;
        System.arraycopy(data, 0, packet, 1, data.length);
        byte checksum = 0;
        for (byte value : packet) checksum ^= value;
        packet[packet.length - 1] = checksum;
        return packet;
    }

    private static byte[] header(int type, int address, int length) {
        ByteBuffer header = ByteBuffer.allocate(17).order(ByteOrder.LITTLE_ENDIAN);
        header.put((byte) type).put(new byte[10]).putShort((short) length).putShort((short) address).putShort((short) 0);
        return packet(0, header.array());
    }

    private static byte[] tap(byte[]... packets) {
        int size = Arrays.stream(packets).mapToInt(p -> p.length + 2).sum();
        ByteBuffer tape = ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN);
        for (byte[] packet : packets) tape.putShort((short) packet.length).put(packet);
        return tape.array();
    }

    private static byte[] tzx(byte[] body) {
        return ByteBuffer.allocate(10 + body.length).put(new byte[]{'Z', 'X', 'T', 'a', 'p', 'e', '!', 0x1A, 1, 20}).put(body).array();
    }
}
