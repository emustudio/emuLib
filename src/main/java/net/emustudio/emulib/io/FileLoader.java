/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Synchronous loader of memory images (BIN, Intel HEX) and tapes (TAP, TZX).
 * <p>
 * A file is split into immutable {@link Block}s which are passed to a {@link Listener} in playback order:
 * TZX jumps, loops and call sequences are followed (the control blocks themselves are passed too).
 * Loading is cancelled by interrupting the loading thread; it then throws {@link InterruptedIOException}.
 * <p>
 * For tape playback, use {@link TapeListener} which decodes tape blocks into signal callbacks.
 */
public final class FileLoader {
    private static final byte[] TZX_SIGNATURE = {'Z', 'X', 'T', 'a', 'p', 'e', '!', 0x1A};

    private FileLoader() {
    }

    /**
     * Supported file formats and their extensions.
     */
    public enum Format {
        BIN(false, "bin", "com", "out", "rom"),
        HEX(false, "hex"),
        TAP(true, "tap"),
        TZX(true, "tzx");

        private final boolean tape;
        private final List<String> extensions;

        Format(boolean tape, String... extensions) {
            this.tape = tape;
            this.extensions = List.of(extensions);
        }

        /**
         * @return true for TAP and TZX
         */
        public boolean isTape() {
            return tape;
        }

        /**
         * @return file extensions without dot
         */
        public List<String> getExtensions() {
            return extensions;
        }

        /**
         * Recognizes format by the last extension of the file name (case-insensitive).
         *
         * @param path file path
         * @return format, or empty if the extension is unknown
         */
        public static Optional<Format> fromPath(Path path) {
            String name = path.getFileName() == null ? "" : path.getFileName().toString();
            int dot = name.lastIndexOf('.');
            String extension = name.substring(dot + 1).toLowerCase(Locale.ROOT);
            return Arrays.stream(values()).filter(f -> dot >= 0 && f.extensions.contains(extension)).findFirst();
        }
    }

    /**
     * Receives blocks synchronously, on the loading thread.
     */
    @FunctionalInterface
    public interface Listener {

        /**
         * Called before the first block; reusable listeners should reset per-file state here.
         *
         * @throws IOException to abort loading
         */
        default void onFileStart() throws IOException {
        }

        /**
         * Called for each block.
         *
         * @param block the block
         * @throws IOException to abort loading
         */
        void onBlock(Block block) throws IOException;

        /**
         * Called only after successful loading, never after failure or cancellation.
         *
         * @throws IOException to abort loading
         */
        default void onFileEnd() throws IOException {
        }
    }

    /**
     * Loads a file and passes its blocks to the listener.
     * <ul>
     *     <li>BIN: one unaddressed block with the whole file</li>
     *     <li>HEX: one addressed block per contiguous address range</li>
     *     <li>TAP: one block (ID 0x10) per tape block</li>
     *     <li>TZX: one block per TZX block, in playback order</li>
     * </ul>
     *
     * @param path     file to load
     * @param format   file format
     * @param listener receiver of blocks
     * @throws IOException if the file cannot be read or is malformed, or the listener fails;
     *                     {@link InterruptedIOException} if the thread was interrupted
     */
    public static void load(Path path, Format format, Listener listener) throws IOException {
        Objects.requireNonNull(path);
        Objects.requireNonNull(format);
        Objects.requireNonNull(listener);
        checkInterrupted();
        List<Block> blocks = (format == Format.HEX) ? readHex(path) : read(Files.readAllBytes(path), format);
        checkInterrupted();
        listener.onFileStart();
        Deque<Integer> calls = new ArrayDeque<>();
        Deque<int[]> loops = new ArrayDeque<>(); // {first block index, remaining count}
        int index = 0;
        while (index < blocks.size()) {
            checkInterrupted();
            Block block = blocks.get(index);
            listener.onBlock(block);
            ByteBuffer body = block.buffer();
            int next = index + 1;
            switch (block.id) {
                case 0x23: // jump
                    int offset = body.getShort();
                    if (offset == 0) throw new IOException("TZX jump to itself");
                    next = index + offset;
                    break;
                case 0x24: // loop start
                    int count = body.getShort() & 0xFFFF;
                    if (count == 0) throw new IOException("TZX loop count must be positive");
                    loops.push(new int[]{index + 1, count});
                    break;
                case 0x25: // loop end
                    if (loops.isEmpty()) throw new IOException("TZX loop end without loop start");
                    if (--loops.peek()[1] > 0) next = loops.peek()[0];
                    else loops.pop();
                    break;
                case 0x26: // call sequence
                    int[] targets = new int[body.getShort() & 0xFFFF];
                    for (int i = 0; i < targets.length; i++) {
                        targets[i] = requireTarget(index + body.getShort(), blocks.size());
                    }
                    if (targets.length > 0) {
                        calls.push(index + 1);
                        for (int i = targets.length - 1; i >= 1; i--) calls.push(targets[i]);
                        next = targets[0];
                    }
                    break;
                case 0x27: // return from sequence
                    if (!calls.isEmpty()) next = calls.pop();
                    break;
                default:
                    break;
            }
            index = requireTarget(next, blocks.size());
        }
        checkInterrupted();
        listener.onFileEnd();
    }

    /**
     * Immutable block of a file.
     * <p>
     * BIN/HEX blocks carry plain bytes. Tape blocks carry an ID and the original block body: TAP blocks have ID 0x10
     * and body = flag, data and checksum; TZX bodies contain all fields after the block ID.
     */
    public static final class Block {
        private final Format format;
        private final int address;
        private final int id;
        private final byte[] bytes;

        Block(Format format, int address, int id, byte[] bytes) {
            this.format = format;
            this.address = address;
            this.id = id;
            this.bytes = bytes;
        }

        /**
         * @return format of the file this block comes from
         */
        public Format getFormat() {
            return format;
        }

        /**
         * @return HEX block address; empty for BIN and tape blocks
         */
        public OptionalInt getAddress() {
            return address < 0 ? OptionalInt.empty() : OptionalInt.of(address);
        }

        /**
         * @return tape block ID (TAP blocks have 0x10), or -1 for BIN/HEX blocks
         */
        public int getId() {
            return id;
        }

        /**
         * Returns BIN/HEX bytes, or a Spectrum data packet (flag, data, checksum) of TAP, TZX 0x10 and TZX 0x11 blocks.
         *
         * @return copy of the data
         * @throws IOException if this tape block has no data packet
         */
        public byte[] getData() throws IOException {
            return Arrays.copyOfRange(bytes, packetOffset(), bytes.length);
        }

        /**
         * @return Spectrum header, if this is a 19-byte data packet with flag 0
         * @throws IOException if this tape block has no data packet
         */
        public Optional<SpectrumHeader> getSpectrumHeader() throws IOException {
            if (!format.isTape()) return Optional.empty();
            byte[] packet = getData();
            if (packet.length != 19 || packet[0] != 0) return Optional.empty();
            return Optional.of(new SpectrumHeader(ByteBuffer.wrap(packet, 1, 17).order(ByteOrder.LITTLE_ENDIAN)));
        }

        /**
         * @return number of used bits in the last byte of the data packet (8 except for TZX turbo blocks)
         * @throws IOException if this tape block has no data packet
         */
        public int getUsedBitsInLastByte() throws IOException {
            return (packetOffset() == 18) ? bytes[12] & 0xFF : 8;
        }

        /**
         * @return true if XOR of the data packet bytes (including flag and checksum) is zero
         * @throws IOException if this is not a tape block with a data packet
         */
        public boolean hasValidChecksum() throws IOException {
            if (!format.isTape()) throw new IOException("Checksum requires a tape data packet");
            int checksum = 0;
            for (byte value : getData()) checksum ^= value;
            return checksum == 0;
        }

        private int packetOffset() throws IOException {
            if (format != Format.TZX) return 0;
            if (id == 0x10) return 4;
            if (id == 0x11) return 18;
            throw new IOException(String.format("Block 0x%02X has no Spectrum data packet", id));
        }

        ByteBuffer buffer() {
            return ByteBuffer.wrap(bytes).asReadOnlyBuffer().order(ByteOrder.LITTLE_ENDIAN);
        }
    }

    /**
     * ZX Spectrum tape header (the 17 bytes after flag 0).
     */
    public static final class SpectrumHeader {
        private final int type;
        private final String fileName;
        private final int dataLength;
        private final int parameter1;
        private final int parameter2;

        private SpectrumHeader(ByteBuffer buffer) {
            this.type = buffer.get() & 0xFF;
            byte[] name = new byte[10];
            buffer.get(name);
            this.fileName = new String(name, StandardCharsets.US_ASCII);
            this.dataLength = buffer.getShort() & 0xFFFF;
            this.parameter1 = buffer.getShort() & 0xFFFF;
            this.parameter2 = buffer.getShort() & 0xFFFF;
        }

        /**
         * @return 0 = program, 1 = number array, 2 = character array, 3 = code (memory block)
         */
        public int getType() {
            return type;
        }

        /**
         * @return file name, padded with spaces to 10 characters
         */
        public String getFileName() {
            return fileName;
        }

        /**
         * @return length of the following data block
         */
        public int getDataLength() {
            return dataLength;
        }

        /**
         * @return autostart line (program), variable name in high byte (arrays) or start address (code)
         */
        public int getParameter1() {
            return parameter1;
        }

        /**
         * @return program length without variables (program) or 32768 (code)
         */
        public int getParameter2() {
            return parameter2;
        }
    }

    private static List<Block> readHex(Path path) throws IOException {
        List<Block> blocks = new ArrayList<>();
        ByteArrayOutputStream run = new ByteArrayOutputStream();
        int start = 0;
        for (Map.Entry<Integer, Byte> entry : IntelHEX.parse(path).getCode().entrySet()) {
            if (entry.getKey() != start + run.size()) {
                if (run.size() > 0) blocks.add(new Block(Format.HEX, start, -1, run.toByteArray()));
                start = entry.getKey();
                run.reset();
            }
            run.write(entry.getValue());
        }
        if (run.size() > 0) blocks.add(new Block(Format.HEX, start, -1, run.toByteArray()));
        return blocks;
    }

    private static List<Block> read(byte[] content, Format format) throws IOException {
        if (format == Format.BIN) return List.of(new Block(format, -1, -1, content));
        ByteBuffer buffer = ByteBuffer.wrap(content).order(ByteOrder.LITTLE_ENDIAN);
        List<Block> blocks = new ArrayList<>();
        try {
            if (format == Format.TZX) {
                if (!Arrays.equals(readBytes(buffer, 8), TZX_SIGNATURE)) throw new IOException("Invalid TZX signature");
                readBytes(buffer, 2); // version
            }
            while (buffer.hasRemaining()) {
                checkInterrupted();
                if (format == Format.TAP) {
                    int length = buffer.getShort() & 0xFFFF;
                    if (length < 2) throw new IOException("Invalid TAP block length: " + length);
                    blocks.add(new Block(format, -1, 0x10, readBytes(buffer, length)));
                } else {
                    int id = buffer.get() & 0xFF;
                    blocks.add(new Block(format, -1, id, readBytes(buffer, tzxBodyLength(buffer, id))));
                }
            }
        } catch (IndexOutOfBoundsException | BufferUnderflowException e) {
            throw new IOException("Incomplete " + format + " block at byte " + buffer.position(), e);
        }
        return blocks;
    }

    // Length of TZX block body (fields after the block ID), from the block's length fields
    private static int tzxBodyLength(ByteBuffer b, int id) throws IOException {
        int p = b.position();
        switch (id) {
            case 0x10: return 4 + (b.getShort(p + 2) & 0xFFFF);
            case 0x11: return 18 + uint24(b, p + 15);
            case 0x12: return 4;
            case 0x13: return 1 + 2 * (b.get(p) & 0xFF);
            case 0x14: return 10 + uint24(b, p + 7);
            case 0x15: return 8 + uint24(b, p + 5);
            case 0x18: case 0x19: case 0x2A: case 0x2B: return 4 + b.getInt(p);
            case 0x20: case 0x23: case 0x24: return 2;
            case 0x21: case 0x30: return 1 + (b.get(p) & 0xFF);
            case 0x22: case 0x25: case 0x27: return 0;
            case 0x26: return 2 + 2 * (b.getShort(p) & 0xFFFF);
            case 0x28: case 0x32: return 2 + (b.getShort(p) & 0xFFFF);
            case 0x31: return 2 + (b.get(p + 1) & 0xFF);
            case 0x33: return 1 + 3 * (b.get(p) & 0xFF);
            case 0x35: return 20 + b.getInt(p + 16);
            case 0x5A: return 9;
            default: throw new IOException(String.format("Unknown TZX block ID 0x%02X at byte %d", id, p - 1));
        }
    }

    static int uint24(ByteBuffer b, int p) {
        return (b.get(p) & 0xFF) | ((b.get(p + 1) & 0xFF) << 8) | ((b.get(p + 2) & 0xFF) << 16);
    }

    static byte[] readBytes(ByteBuffer buffer, int count) throws IOException {
        if (count < 0 || count > buffer.remaining()) throw new IOException("Invalid or incomplete tape block length: " + count);
        byte[] bytes = new byte[count];
        buffer.get(bytes);
        return bytes;
    }

    private static int requireTarget(int target, int size) throws IOException {
        if (target < 0 || target > size) throw new IOException("TZX target outside tape: " + target);
        return target;
    }

    private static void checkInterrupted() throws InterruptedIOException {
        if (Thread.currentThread().isInterrupted()) throw new InterruptedIOException("File loading cancelled");
    }
}
