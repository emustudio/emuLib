/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.io;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.BooleanSupplier;

/**
 * Common synchronous loader contract for BIN, I8HEX, TAP and TZX. Format instances are
 * stateless and reusable. It emits immutable blocks; callers decide how to use their bytes and metadata.
 */
public interface FileLoader {
    Format getFormat();

    static FileLoader forFormat(Format format) {
        return Objects.requireNonNull(format);
    }

    /**
     * Unknown extensions have no fallback; callers can explicitly select BIN.
     */
    static Optional<FileLoader> forPath(Path path) {
        return Format.fromPath(path).map(FileLoader::forFormat);
    }

    /**
     * Emits blocks to the listener; completion is omitted on failure or cancellation.
     */
    default void load(Path path, Listener listener, Options options) throws IOException {
        Objects.requireNonNull(path);
        Objects.requireNonNull(listener);
        Objects.requireNonNull(options);
        options.checkCancelled();
        Format format = getFormat();
        switch (format) {
            case BIN:
                byte[] data = Files.readAllBytes(path);
                options.checkCancelled();
                listener.onFileStart();
                listener.onBlock(Block.data(format, -1, data));
                break;
            case HEX:
                // Retain IntelHEX's I8HEX scope, overlap resolution and checksum policy.
                Map<Integer, Byte> code = IntelHEX.parse(path.toFile()).getCode();
                options.checkCancelled();
                listener.onFileStart();
                for (Map.Entry<Integer, Byte> entry : code.entrySet()) {
                    options.checkCancelled();
                    listener.onBlock(Block.data(format, entry.getKey(), new byte[]{entry.getValue()}));
                }
                break;
            case TAP:
            case TZX:
                TapeReader.load(path, format, listener, options);
                break;
            default:
                throw new IOException("Unsupported format: " + format);
        }
        options.checkCancelled();
        listener.onFileEnd();
    }

    /**
     * File encodings and their extensions.
     */
    enum Format implements FileLoader {
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

        @Override
        public Format getFormat() {
            return this;
        }

        public boolean isTape() {
            return tape;
        }

        public List<String> getExtensions() {
            return extensions;
        }

        /**
         * Recognizes the last extension of the filename, independent of parent directories.
         */
        public static Optional<Format> fromPath(Path path) {
            String name = path.getFileName() == null ? "" : path.getFileName().toString();
            int dot = name.lastIndexOf('.');
            String extension = dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
            for (Format format : values()) {
                if (format.extensions.contains(extension)) return Optional.of(format);
            }
            return Optional.empty();
        }
    }

    /**
     * Per-load traversal and cancellation. Thread interruption is always honored.
     */
    final class Options {
        public enum Traversal {FILE_ORDER, PLAYBACK_ORDER}

        private final Traversal traversal;
        private final BooleanSupplier cancelled;

        private Options(Traversal traversal, BooleanSupplier cancelled) {
            this.traversal = traversal;
            this.cancelled = cancelled;
        }

        /**
         * Visits each physical block once.
         */
        public static Options fileOrder() {
            return new Options(Traversal.FILE_ORDER, () -> false);
        }

        /**
         * Tape traversal honoring TZX jumps, loops and call sequences.
         */
        public static Options playback() {
            return new Options(Traversal.PLAYBACK_ORDER, () -> false);
        }

        /**
         * Thread interruption is always honored, in addition to this cancellation signal.
         */
        public Options withCancellation(BooleanSupplier cancelled) {
            return new Options(traversal, Objects.requireNonNull(cancelled));
        }

        public Traversal getTraversal() {
            return traversal;
        }

        void checkCancelled() throws InterruptedIOException {
            if (Thread.currentThread().isInterrupted() || cancelled.getAsBoolean()) {
                throw new InterruptedIOException("File loading cancelled");
            }
        }
    }

    /**
     * Immutable format block. HEX has embedded addresses; BIN has unaddressed bytes. TAP/TZX have an ID and
     * original body bytes. TAP uses ID 0x10 and its body includes flag/data/checksum; TZX bodies
     * contain all fields after the block ID. Format determines which representation applies.
     */
    final class Block {
        private final Format format;
        private final int address;
        private final int id;
        private final byte[] bytes;

        private Block(Format format, int address, int id, byte[] bytes) {
            this.format = format;
            this.address = address;
            this.id = id;
            this.bytes = bytes.clone();
        }

        static Block data(Format format, int address, byte[] data) {
            return new Block(format, address, -1, data);
        }

        static Block tape(Format format, int id, byte[] body) {
            return new Block(format, -1, id, body);
        }

        public Format getFormat() {
            return format;
        }

        /**
         * Embedded HEX address; empty for BIN and tape blocks.
         */
        public OptionalInt getAddress() {
            return address < 0 ? OptionalInt.empty() : OptionalInt.of(address);
        }

        /**
         * Tape block ID, or -1 for BIN/HEX bytes.
         */
        public int getId() {
            return id;
        }

        public byte[] getBytes() {
            return bytes.clone();
        }

        ByteBuffer buffer() {
            return ByteBuffer.wrap(bytes).asReadOnlyBuffer().order(ByteOrder.LITTLE_ENDIAN);
        }

        /**
         * BIN/HEX bytes or a standard/turbo Spectrum packet including flag and checksum.
         */
        public byte[] getData() throws IOException {
            if (!format.isTape() || format == Format.TAP) return bytes.clone();
            ByteBuffer buffer = buffer();
            int count;
            if (id == 0x10) {
                buffer.getShort();
                count = buffer.getShort() & 0xFFFF;
            } else if (id == 0x11) {
                buffer.position(15);
                count = TapeReader.readUint24(buffer);
            } else {
                throw new IOException(String.format("Block 0x%02X has no Spectrum data packet", id));
            }
            return TapeReader.readBytes(buffer, count);
        }

        /** Spectrum header metadata, if this packet has flag zero and a 17-byte header. */
        public Optional<SpectrumHeader> getSpectrumHeader() throws IOException {
            if (!format.isTape()) return Optional.empty();
            if (format == Format.TZX && id != 0x10 && id != 0x11) return Optional.empty();
            byte[] packet = getData();
            if (packet.length != 19 || packet[0] != 0) return Optional.empty();
            return Optional.of(SpectrumHeader.parse(ByteBuffer.wrap(packet, 1, 17)));
        }

        /** Number of meaningful bits in the last byte of a standard/turbo packet. */
        public int getUsedBitsInLastByte() throws IOException {
            if (format == Format.TAP || (format == Format.TZX && id == 0x10)) return 8;
            if (format == Format.TZX && id == 0x11) return bytes[12] & 0xFF;
            throw new IOException("Block has no standard/turbo data packet");
        }

        /**
         * XOR check of a Spectrum packet, including original flag and checksum.
         */
        public boolean hasValidChecksum() throws IOException {
            if (!format.isTape()) throw new IOException("Checksum requires a tape data packet");
            int checksum = 0;
            for (byte value : getData()) checksum ^= value & 0xFF;
            return checksum == 0;
        }
    }

    /** Spectrum header fields decoded independently of their destination. */
    final class SpectrumHeader {
        private final int id;
        private final String fileName;
        private final int dataLength;
        private final int parameter1;
        private final int parameter2;

        private SpectrumHeader(int id, String fileName, int dataLength, int parameter1, int parameter2) {
            this.id = id;
            this.fileName = fileName;
            this.dataLength = dataLength;
            this.parameter1 = parameter1;
            this.parameter2 = parameter2;
        }

        public int getType() {
            return id;
        }

        public String getFileName() {
            return fileName;
        }

        public int getDataLength() {
            return dataLength;
        }

        public int getParameter1() {
            return parameter1;
        }

        public int getParameter2() {
            return parameter2;
        }

        public char getVariable() {
            return (char) ((parameter1 >>> 8) & 0xFF);
        }

        static SpectrumHeader parse(ByteBuffer buffer) {
            buffer = buffer.slice().order(ByteOrder.LITTLE_ENDIAN);
            int headerFlag = buffer.get() & 0xFF;
            byte[] fileName = new byte[10];
            buffer.get(fileName); // filename
            int dataLength = buffer.getShort() & 0xFFFF; // length

            int parameter1 = buffer.getShort() & 0xFFFF;
            int parameter2 = buffer.getShort() & 0xFFFF;

            return new SpectrumHeader(headerFlag, new String(fileName, StandardCharsets.US_ASCII), dataLength, parameter1, parameter2);
        }
    }

    /**
     * Receives blocks synchronously; a listener need not be thread-safe.
     */
    @FunctionalInterface
    interface Listener {
        void onBlock(Block block) throws IOException;

        /**
         * Called before the first block; reusable listeners should reset per-file state here.
         */
        default void onFileStart() throws IOException {
        }

        /**
         * Called only after successful traversal, never after failure or cancellation.
         */
        default void onFileEnd() throws IOException {
        }
    }
}
