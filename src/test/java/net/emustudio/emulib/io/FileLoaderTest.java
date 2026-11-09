/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.io;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.emustudio.emulib.io.FileLoader.Format.*;
import static org.junit.Assert.*;

public class FileLoaderTest {
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void recognizesFormatsByExtension() {
        for (String extension : List.of("bin", "com", "out", "rom", "BIN")) {
            assertEquals(BIN, FileLoader.Format.fromPath(Path.of("x." + extension)).orElseThrow());
        }
        assertEquals(HEX, FileLoader.Format.fromPath(Path.of("x.HeX")).orElseThrow());
        assertEquals(TZX, FileLoader.Format.fromPath(Path.of("a.b.tzx")).orElseThrow());
        assertTrue(FileLoader.Format.fromPath(Path.of("x.unknown")).isEmpty());
        assertTrue(FileLoader.Format.fromPath(Path.of("directory.tap/tap")).isEmpty());
        assertFalse(BIN.isTape());
        assertTrue(TAP.isTape());
        assertEquals(List.of("tzx"), TZX.getExtensions());
    }

    @Test
    public void binaryIsOneUnaddressedBlockOwningItsBytes() throws IOException {
        List<FileLoader.Block> blocks = load(file("image.bin", new byte[]{1, 2, 3}), BIN);
        assertEquals(1, blocks.size());
        FileLoader.Block block = blocks.get(0);
        assertTrue(block.getAddress().isEmpty());
        assertEquals(-1, block.getId());
        block.getData()[0] = 99;
        assertArrayEquals(new byte[]{1, 2, 3}, block.getData());
        assertTrue(block.getSpectrumHeader().isEmpty());
        assertThrows(IOException.class, block::hasValidChecksum);
    }

    @Test
    public void hexIsSplitIntoContiguousRangesWithLastOverlappingRecord() throws IOException {
        Path path = tmp.newFile("sparse.hex").toPath();
        Files.writeString(path, ":020010001122BB\n:0100200033AC\n:0100110044AA\n:010012005598\n:00000001FF\n");
        Map<Integer, String> ranges = new TreeMap<>();
        for (FileLoader.Block block : load(path, HEX)) {
            ranges.put(block.getAddress().orElseThrow(), hex(block.getData()));
        }
        assertEquals(Map.of(0x10, "114455", 0x20, "33"), ranges);
    }

    @Test
    public void tapBlocksExposePacketsAndSpectrumHeaders() throws IOException {
        byte[] data = packet(255, 0x55, 0x66);
        List<FileLoader.Block> blocks = load(file("code.tap", tap(header(3, 0x1234, 2), data)), TAP);
        assertEquals(2, blocks.size());
        FileLoader.SpectrumHeader header = blocks.get(0).getSpectrumHeader().orElseThrow();
        assertEquals(3, header.getType());
        assertEquals("name      ", header.getFileName());
        assertEquals(2, header.getDataLength());
        assertEquals(0x1234, header.getParameter1());
        assertEquals(0x8000, header.getParameter2());
        assertEquals(0x10, blocks.get(0).getId());
        assertEquals(8, blocks.get(0).getUsedBitsInLastByte());
        assertTrue(blocks.get(0).getAddress().isEmpty());
        assertTrue(blocks.get(1).getSpectrumHeader().isEmpty());
        assertArrayEquals(data, blocks.get(1).getData());
        assertTrue(blocks.get(1).hasValidChecksum());

        data[data.length - 1] ^= 1;
        assertFalse(load(file("corrupt.tap", tap(data)), TAP).get(0).hasValidChecksum());
    }

    @Test
    public void tzxDataBlocksExposePacketsAndOtherBlocksDoNot() throws IOException {
        byte[] packet = packet(255, 1, 2);
        Path path = file("data.tzx", tzx(new Tape()
                .b(0x10).w(1000).w(packet.length).b(packet)
                .b(0x11).w(1, 2, 3, 4, 5, 6).b(6).w(0).t(packet.length).b(packet)
                .b(0x12).w(2168, 10)));
        List<FileLoader.Block> blocks = load(path, TZX);
        assertArrayEquals(packet, blocks.get(0).getData());
        assertEquals(8, blocks.get(0).getUsedBitsInLastByte());
        assertArrayEquals(packet, blocks.get(1).getData());
        assertEquals(6, blocks.get(1).getUsedBitsInLastByte());
        assertEquals(0x12, blocks.get(2).getId());
        assertThrows(IOException.class, () -> blocks.get(2).getData());
        assertThrows(IOException.class, () -> blocks.get(2).getUsedBitsInLastByte());
    }

    @Test
    public void malformedFilesFailWithIoException() throws IOException {
        for (byte[] content : List.of(new byte[]{1}, new byte[]{1, 0, 0}, new byte[]{5, 0, 0, 1})) {
            Path path = file("bad" + tmp.getRoot().list().length + ".tap", content);
            assertThrows(IOException.class, () -> load(path, TAP));
        }
        List<byte[]> tzxFiles = List.of(
                "INVALID\u001A\u0001\u0014".getBytes(), // signature
                "ZXTape!\u0000\u0001\u0014".getBytes(), // end-of-text marker
                "ZXTape!\u001A\u0001".getBytes(), // truncated version
                tzx(new Tape().b(0x7F)), // unknown block
                tzx(new Tape().b(0x11, 0)), // truncated block
                tzx(new Tape().b(0x35).b(new byte[16]).d(-1)), // negative length
                tzx(new Tape().b(0x23).w(0)), // jump to itself
                tzx(new Tape().b(0x23).w(5)), // jump outside the tape
                tzx(new Tape().b(0x25)), // loop end without start
                tzx(new Tape().b(0x24).w(0).b(0x25))); // zero loop count
        for (byte[] content : tzxFiles) {
            Path path = file("bad" + tmp.getRoot().list().length + ".tzx", content);
            assertThrows(IOException.class, () -> load(path, TZX));
        }
        assertThrows(IOException.class, () -> load(tmp.getRoot().toPath().resolve("missing.tzx"), TZX));
        assertThrows(NullPointerException.class, () -> load(null, TZX));
    }

    @Test
    public void emptyTzxHasNoBlocks() throws IOException {
        assertTrue(load(file("empty.tzx", tzx(new Tape())), TZX).isEmpty());
    }

    @Test
    public void tzxJumpsLoopsAndCallSequencesAreFollowed() throws IOException {
        Path jump = file("jump.tzx", tzx(new Tape().b(0x23).w(2).b(0x12).w(1, 1).b(0x12).w(2, 1)));
        assertEquals(List.of("23", "12:2"), trace(jump));

        Path loops = file("loops.tzx", tzx(new Tape().b(0x24).w(2).b(0x24).w(3).b(0x12).w(1, 1).b(0x25).b(0x25)));
        assertEquals(6, trace(loops).stream().filter(s -> s.startsWith("12")).count());

        // Call blocks 2 and 4, return after each; main path then jumps to EOF.
        Path calls = file("calls.tzx", tzx(new Tape().b(0x26).w(2, 2, 4).b(0x23).w(5)
                .b(0x12).w(1, 1).b(0x27).b(0x12).w(2, 1).b(0x27)));
        assertEquals(List.of("26", "12:1", "27", "12:2", "27", "23"), trace(calls));
    }

    @Test
    public void interruptionAndListenerFailuresOmitCompletion() throws IOException {
        Path path = file("file.bin", new byte[]{1});
        AtomicBoolean completed = new AtomicBoolean();
        FileLoader.Listener failing = new FileLoader.Listener() {
            @Override
            public void onBlock(FileLoader.Block block) throws IOException {
                throw new IOException("Listener failed");
            }

            @Override
            public void onFileEnd() {
                completed.set(true);
            }
        };
        assertThrows(IOException.class, () -> FileLoader.load(path, BIN, failing));
        Thread.currentThread().interrupt();
        try {
            assertThrows(InterruptedIOException.class, () -> FileLoader.load(path, BIN, b -> completed.set(true)));
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
        assertFalse(completed.get());
    }

    @Test
    public void interruptionCancelsEndlessTape() throws IOException {
        Path path = file("endless.tzx", tzx(new Tape().b(0x12).w(1, 1).b(0x23).w(-1)));
        List<Integer> visits = new ArrayList<>();
        assertThrows(InterruptedIOException.class, () -> FileLoader.load(path, TZX, block -> {
            visits.add(block.getId());
            if (visits.size() == 4) Thread.currentThread().interrupt();
        }));
        Thread.interrupted();
        assertEquals(List.of(0x12, 0x23, 0x12, 0x23), visits);
    }

    private List<String> trace(Path path) throws IOException {
        List<String> trace = new ArrayList<>();
        FileLoader.load(path, TZX, block -> trace.add(String.format("%02X", block.getId())
                + (block.getId() == 0x12 ? ":" + block.buffer().getShort() : "")));
        return trace;
    }

    private static List<FileLoader.Block> load(Path path, FileLoader.Format format) throws IOException {
        List<FileLoader.Block> blocks = new ArrayList<>();
        AtomicBoolean started = new AtomicBoolean();
        AtomicBoolean ended = new AtomicBoolean();
        FileLoader.load(path, format, new FileLoader.Listener() {
            @Override
            public void onFileStart() {
                started.set(true);
            }

            @Override
            public void onBlock(FileLoader.Block block) {
                assertTrue(started.get());
                blocks.add(block);
            }

            @Override
            public void onFileEnd() {
                ended.set(true);
            }
        });
        assertTrue(ended.get());
        return blocks;
    }

    private Path file(String name, byte[] content) throws IOException {
        return Files.write(tmp.newFile(name).toPath(), content);
    }

    static String hex(byte[] data) {
        StringBuilder result = new StringBuilder();
        for (byte b : data) result.append(String.format("%02X", b));
        return result.toString();
    }

    static byte[] packet(int flag, int... data) {
        byte[] packet = new byte[data.length + 2];
        packet[0] = (byte) flag;
        for (int i = 0; i < data.length; i++) packet[i + 1] = (byte) data[i];
        byte checksum = 0;
        for (byte value : packet) checksum ^= value;
        packet[packet.length - 1] = checksum;
        return packet;
    }

    static byte[] header(int type, int parameter1, int length) {
        ByteBuffer header = ByteBuffer.allocate(17).order(ByteOrder.LITTLE_ENDIAN);
        header.put((byte) type).put("name      ".getBytes()).putShort((short) length)
                .putShort((short) parameter1).putShort((short) 0x8000);
        int[] data = new int[17];
        for (int i = 0; i < 17; i++) data[i] = header.get(i);
        return packet(0, data);
    }

    static byte[] tap(byte[]... packets) {
        Tape tape = new Tape();
        for (byte[] packet : packets) tape.w(packet.length).b(packet);
        return tape.bytes();
    }

    static byte[] tzx(Tape blocks) {
        return new Tape().b("ZXTape!".getBytes()).b(0x1A, 1, 20).b(blocks.bytes()).bytes();
    }

    /**
     * Little-endian tape content builder.
     */
    static final class Tape {
        private final ByteArrayOutputStream out = new ByteArrayOutputStream();

        Tape b(int... values) {
            for (int v : values) out.write(v);
            return this;
        }

        Tape b(byte[] values) {
            out.writeBytes(values);
            return this;
        }

        Tape w(int... words) {
            for (int v : words) b(v, v >> 8);
            return this;
        }

        Tape t(int value) {
            return b(value, value >> 8, value >> 16);
        }

        Tape d(int value) {
            return w(value, value >> 16);
        }

        Tape s(String text) {
            return b(text.getBytes());
        }

        byte[] bytes() {
            return out.toByteArray();
        }
    }
}
