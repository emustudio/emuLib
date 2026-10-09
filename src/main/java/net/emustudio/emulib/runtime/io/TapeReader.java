/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.io;

import java.io.IOException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.emustudio.emulib.runtime.io.FileLoader.*;

/** Shared physical tape parsing and optional TZX control-flow traversal. */
final class TapeReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(TapeReader.class);
    private static final String[] ARCHIVE_INFO_TYPES = {
        "Title", "Publisher", "Author", "Publication year", "Language", "Type", "Price", "Protection", "Origin", "Comment"
    };

    private static final byte[] SIGNATURE = {'Z', 'X', 'T', 'a', 'p', 'e', '!', 0x1A};

    static void load(Path path, Format format, Listener listener, Options options) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(Files.readAllBytes(path)).order(ByteOrder.LITTLE_ENDIAN);
        options.checkCancelled();
        List<Block> blocks = new ArrayList<>();
        try {
            if (format == Format.TZX) {
                if (!Arrays.equals(readBytes(buffer, 8), SIGNATURE)) throw new IOException("Invalid TZX signature");
                skip(buffer, 2); // version
            }
            while (buffer.hasRemaining()) {
                options.checkCancelled();
                int id;
                byte[] body;
                if (format == Format.TAP) {
                    id = 0x10;
                    int length = buffer.getShort() & 0xFFFF;
                    if (length < 2) throw new IOException("Invalid TAP block length: " + length);
                    body = readBytes(buffer, length);
                } else {
                    id = buffer.get() & 0xFF;
                    int start = buffer.position();
                    skipBlockData(buffer, id);
                    int end = buffer.position();
                    buffer.position(start);
                    body = readBytes(buffer, end - start);
                }
                blocks.add(Block.tape(format, id, body));
            }
        } catch (BufferUnderflowException | IllegalArgumentException e) {
            throw new IOException("Incomplete " + format + " block at byte " + buffer.position(), e);
        }
        options.checkCancelled();
        listener.onFileStart();
        if (options.getTraversal() == Options.Traversal.FILE_ORDER) {
            for (Block block : blocks) {
                options.checkCancelled();
                listener.onBlock(block);
            }
        } else {
            interpret(blocks, listener, options);
        }
    }

    private static void interpret(List<Block> blocks, Listener listener, Options options) throws IOException {
        int index = 0;
        Deque<Integer> calls = new ArrayDeque<>();
        Deque<int[]> loops = new ArrayDeque<>();
        while (index < blocks.size()) {
            options.checkCancelled();
            Block block = blocks.get(index);
            ByteBuffer body = block.buffer();
            listener.onBlock(block);
            int next = index + 1;
            switch (block.getId()) {
                case 0x23:
                    next = index + body.getShort();
                    break;
                case 0x24:
                    int count = body.getShort() & 0xFFFF;
                    if (count == 0) throw new IOException("TZX loop count must be positive");
                    loops.push(new int[]{index + 1, count});
                    break;
                case 0x25:
                    if (loops.isEmpty()) throw new IOException("TZX loop end without loop start");
                    int[] loop = loops.peek();
                    if (--loop[1] > 0) next = loop[0];
                    else loops.pop();
                    break;
                case 0x26:
                    int numCalls = body.getShort() & 0xFFFF;
                    int[] targets = new int[numCalls];
                    for (int i = 0; i < numCalls; i++) {
                        targets[i] = index + body.getShort();
                        requireTarget(targets[i], blocks.size());
                    }
                    if (numCalls > 0) {
                        calls.push(index + 1);
                        for (int i = numCalls - 1; i >= 1; i--) calls.push(targets[i]);
                        next = targets[0];
                    }
                    break;
                case 0x27:
                    if (!calls.isEmpty()) next = calls.pop();
                    break;
                default:
                    break;
            }
            requireTarget(next, blocks.size());
            index = next;
        }
    }

    private static void requireTarget(int target, int size) throws IOException {
        if (target < 0 || target > size) throw new IOException("TZX target outside tape: " + target);
    }

    static int readUint24(ByteBuffer buffer) {
        return (buffer.get() & 0xFF) | ((buffer.get() & 0xFF) << 8) | ((buffer.get() & 0xFF) << 16);
    }

    static byte[] readBytes(ByteBuffer buffer, int count) throws IOException {
        requireRemaining(buffer, count);
        byte[] bytes = new byte[count];
        buffer.get(bytes);
        return bytes;
    }

    private static void requireRemaining(ByteBuffer buffer, int count) throws IOException {
        if (count < 0 || count > buffer.remaining()) throw new IOException("Invalid or incomplete tape block length: " + count);
    }

    private static void skip(ByteBuffer buffer, int count) throws IOException {
        requireRemaining(buffer, count);
        buffer.position(buffer.position() + count);
    }

    private static void skipBlockData(ByteBuffer buffer, int id) throws IOException {
        switch (id) {
            case 0x10: { // Standard Speed Data
                buffer.getShort(); // pause
                int len = buffer.getShort() & 0xFFFF;
                skip(buffer, len);
                break;
            }
            case 0x11: { // Turbo Speed Data
                skip(buffer, 15); // fixed fields
                int len = readUint24(buffer);
                skip(buffer, len);
                break;
            }
            case 0x12: // Pure Tone
                skip(buffer, 4);
                break;
            case 0x13: { // Pulse Sequence
                int count = buffer.get() & 0xFF;
                skip(buffer, count * 2);
                break;
            }
            case 0x14: { // Pure Data
                skip(buffer, 7); // fixed fields
                int len = readUint24(buffer);
                skip(buffer, len);
                break;
            }
            case 0x15: { // Direct Recording
                skip(buffer, 5); // fixed fields
                int len = readUint24(buffer);
                skip(buffer, len);
                break;
            }
            case 0x18: // CSW Recording
            case 0x19: { // Generalized Data
                int len = buffer.getInt();
                if (len < (id == 0x18 ? 10 : 2)) throw new IOException("Invalid recording block length");
                skip(buffer, len);
                break;
            }
            case 0x20: // Pause
                buffer.getShort();
                break;
            case 0x21: { // Group Start
                int len = buffer.get() & 0xFF;
                skip(buffer, len);
                break;
            }
            case 0x22: // Group End
            case 0x25: // Loop End
            case 0x27: // Return from Sequence
                // no data
                break;
            case 0x23: // Jump to Block
            case 0x24: // Loop Start
                buffer.getShort();
                break;
            case 0x26: { // Call Sequence
                int count = buffer.getShort() & 0xFFFF;
                skip(buffer, count * 2);
                break;
            }
            case 0x28: { // Select Block
                int len = buffer.getShort() & 0xFFFF;
                skip(buffer, len);
                break;
            }
            case 0x2A: // Stop if in 48K mode
                if (buffer.getInt() != 0) throw new IOException("Invalid stop block length");
                break;
            case 0x2B: // Set Signal Level
                if (buffer.getInt() != 1) throw new IOException("Invalid signal block length");
                buffer.get();    // signal level
                break;
            case 0x30: { // Text Description
                int len = buffer.get() & 0xFF;
                skip(buffer, len);
                break;
            }
            case 0x31: { // Message
                buffer.get(); // display time
                int len = buffer.get() & 0xFF;
                skip(buffer, len);
                break;
            }
            case 0x32: { // Archive Info
                int len = buffer.getShort() & 0xFFFF;
                skip(buffer, len);
                break;
            }
            case 0x33: { // Hardware Type
                int count = buffer.get() & 0xFF;
                skip(buffer, count * 3);
                break;
            }
            case 0x35: { // Custom Info
                skip(buffer, 16); // ID string
                int len = buffer.getInt();
                skip(buffer, len);
                break;
            }
            case 0x5A: // Glue
                skip(buffer, 9);
                break;
            default:
                throw new IOException(String.format("TZX: Unknown block ID 0x%02X at position %d - cannot skip",
                        id, buffer.position() - 1));
        }
    }

    static void emit(Block block, TapeListener listener) throws IOException {
        if (!block.getFormat().isTape()) throw new IOException("Playback requires a tape block");
        Block tape = block;
        ByteBuffer buffer = tape.buffer();
        try {
            switch (tape.getId()) {
                case 0x10: standardData(tape, listener); break;
                case 0x11: parseBlock11(buffer, listener); break;
                case 0x12: parseBlock12(buffer, listener); break;
                case 0x13: parseBlock13(buffer, listener); break;
                case 0x14: parseBlock14(buffer, listener); break;
                case 0x15: parseBlock15(buffer, listener); break;
                case 0x18: parseBlock18(buffer, listener); break;
                case 0x19: parseBlock19(buffer, listener); break;
                case 0x20: parseBlock20(buffer, listener); break;
                case 0x21: parseBlock21(buffer, listener); break;
                case 0x22: listener.onGroupEnd(); break;
                case 0x28: parseBlock28(buffer); break;
                case 0x2A: parseBlock2A(buffer, listener); break;
                case 0x2B: parseBlock2B(buffer, listener); break;
                case 0x30: parseBlock30(buffer, listener); break;
                case 0x31: parseBlock31(buffer, listener); break;
                case 0x32: parseBlock32(buffer, listener); break;
                case 0x33: parseBlock33(buffer, listener); break;
                case 0x35: parseBlock35(buffer, listener); break;
                case 0x5A: parseBlock5A(buffer, listener); break;
                default: break; // control flow belongs to the loader
            }
        } catch (BufferUnderflowException | IllegalArgumentException e) {
            throw new IOException(String.format("Malformed tape block 0x%02X", tape.getId()), e);
        }
    }

    private static void standardData(Block block, TapeListener listener) throws IOException {
        byte[] packet = block.getData();
        if (packet.length < 2) throw new IOException("Tape data requires flag and checksum");
        int flag = packet[0] & 0xFF;
        byte[] data = Arrays.copyOfRange(packet, 1, packet.length - 1);
        if (block.getFormat() == Format.TAP && !block.hasValidChecksum()) {
            LOGGER.error("Tape checksum is wrong");
        }
        if (flag < 0x80) {
            listener.onHeaderStart();
            // Preserve callback ordering used by existing playback clients.
            if (block.getFormat() == Format.TZX) listener.onBlockFlag(flag);
            if (data.length == 17) {
                SpectrumHeader header = SpectrumHeader.parse(ByteBuffer.wrap(data));
                switch (header.getType()) {
                    case 0: listener.onProgram(header.getFileName(), header.getDataLength(), header.getParameter1(), header.getParameter2()); break;
                    case 1: listener.onNumberArray(header.getFileName(), header.getDataLength(), header.getVariable()); break;
                    case 2: listener.onStringArray(header.getFileName(), header.getDataLength(), header.getVariable()); break;
                    case 3: listener.onMemoryBlock(header.getFileName(), header.getDataLength(), header.getParameter1()); break;
                    default: LOGGER.warn("Unknown Spectrum header ID: {}", header.getType());
                }
            }
            if (block.getFormat() == Format.TAP) listener.onBlockFlag(flag);
        } else {
            listener.onDataStart();
            listener.onBlockFlag(flag);
        }
        listener.onBlockData(data);
        listener.onBlockChecksum(packet[packet.length - 1]);
        if (block.getFormat() == Format.TZX) {
            int pause = block.buffer().getShort() & 0xFFFF;
            if (pause > 0) listener.onPause(pause);
        }
    }

    /**
     * Block 0x11 - Turbo Speed Data Block.
     */
    private static void parseBlock11(ByteBuffer buffer, TapeListener listener) throws IOException {
        int pilotPulseLen = buffer.getShort() & 0xFFFF;
        int sync1PulseLen = buffer.getShort() & 0xFFFF;
        int sync2PulseLen = buffer.getShort() & 0xFFFF;
        int zeroBitPulseLen = buffer.getShort() & 0xFFFF;
        int oneBitPulseLen = buffer.getShort() & 0xFFFF;
        int pilotToneCount = buffer.getShort() & 0xFFFF;
        int usedBitsInLastByte = buffer.get() & 0xFF;
        int pauseAfterMs = buffer.getShort() & 0xFFFF;
        int dataLength = TapeReader.readUint24(buffer);

        byte[] data = TapeReader.readBytes(buffer, dataLength);

        listener.onTurboSpeedData(pilotPulseLen, sync1PulseLen, sync2PulseLen,
                zeroBitPulseLen, oneBitPulseLen, pilotToneCount,
                usedBitsInLastByte, pauseAfterMs, data);
    }

    /**
     * Block 0x12 - Pure Tone.
     */
    private static void parseBlock12(ByteBuffer buffer, TapeListener listener) throws IOException {
        int pulseLength = buffer.getShort() & 0xFFFF;
        int pulseCount = buffer.getShort() & 0xFFFF;
        listener.onPureTone(pulseLength, pulseCount);
    }

    /**
     * Block 0x13 - Pulse Sequence.
     */
    private static void parseBlock13(ByteBuffer buffer, TapeListener listener) throws IOException {
        int count = buffer.get() & 0xFF;
        int[] pulseLengths = new int[count];
        for (int i = 0; i < count; i++) {
            pulseLengths[i] = buffer.getShort() & 0xFFFF;
        }
        listener.onPulseSequence(pulseLengths);
    }

    /**
     * Block 0x14 - Pure Data Block.
     */
    private static void parseBlock14(ByteBuffer buffer, TapeListener listener) throws IOException {
        int zeroBitPulseLen = buffer.getShort() & 0xFFFF;
        int oneBitPulseLen = buffer.getShort() & 0xFFFF;
        int usedBitsInLastByte = buffer.get() & 0xFF;
        int pauseAfterMs = buffer.getShort() & 0xFFFF;
        int dataLength = TapeReader.readUint24(buffer);

        byte[] data = TapeReader.readBytes(buffer, dataLength);

        listener.onPureData(zeroBitPulseLen, oneBitPulseLen, usedBitsInLastByte, pauseAfterMs, data);
    }

    /**
     * Block 0x15 - Direct Recording.
     */
    private static void parseBlock15(ByteBuffer buffer, TapeListener listener) throws IOException {
        int tstatesPerSample = buffer.getShort() & 0xFFFF;
        int pauseAfterMs = buffer.getShort() & 0xFFFF;
        int usedBitsInLastByte = buffer.get() & 0xFF;
        int dataLength = TapeReader.readUint24(buffer);

        byte[] samples = TapeReader.readBytes(buffer, dataLength);

        listener.onDirectRecording(tstatesPerSample, pauseAfterMs, usedBitsInLastByte, samples);
    }

    /**
     * Block 0x18 - CSW Recording.
     */
    private static void parseBlock18(ByteBuffer buffer, TapeListener listener) throws IOException {
        int blockLength = buffer.getInt();
        int pauseAfterMs = buffer.getShort() & 0xFFFF;
        int sampleRate = TapeReader.readUint24(buffer);
        int compressionType = buffer.get() & 0xFF;
        long storedPulseCount = buffer.getInt() & 0xFFFFFFFFL;
        int dataLength = blockLength - 10;

        byte[] data = TapeReader.readBytes(buffer, dataLength);

        listener.onCswRecording(pauseAfterMs, sampleRate, compressionType, storedPulseCount, data);
    }

    /**
     * Block 0x19 - Generalized Data Block.
     */
    private static void parseBlock19(ByteBuffer buffer, TapeListener listener) throws IOException {
        int blockLength = buffer.getInt();
        int pauseAfterMs = buffer.getShort() & 0xFFFF;
        int remainingLength = blockLength - 2;

        byte[] data = TapeReader.readBytes(buffer, remainingLength);

        listener.onGeneralizedData(pauseAfterMs, data);
    }

    /**
     * Block 0x20 - Pause (Silence) or Stop the Tape.
     */
    private static void parseBlock20(ByteBuffer buffer, TapeListener listener) throws IOException {
        int durationMs = buffer.getShort() & 0xFFFF;
        listener.onPause(durationMs);
    }

    /**
     * Block 0x21 - Group Start.
     */
    private static void parseBlock21(ByteBuffer buffer, TapeListener listener) throws IOException {
        int len = buffer.get() & 0xFF;
        byte[] name = TapeReader.readBytes(buffer, len);
        listener.onGroupStart(new String(name, StandardCharsets.US_ASCII));
    }

    /**
     * Block 0x28 - Select Block (interactive - skipped with warning).
     */
    private static void parseBlock28(ByteBuffer buffer) throws IOException {
        int len = buffer.getShort() & 0xFFFF;
        buffer.position(buffer.position() + len);
        LOGGER.warn("TZX: Select Block (0x28) is not supported, skipping");
    }

    /**
     * Block 0x2A - Stop the tape if in 48K mode.
     */
    private static void parseBlock2A(ByteBuffer buffer, TapeListener listener) throws IOException {
        buffer.getInt(); // always 0
        listener.onStopIfIn48KMode();
    }

    /**
     * Block 0x2B - Set Signal Level.
     */
    private static void parseBlock2B(ByteBuffer buffer, TapeListener listener) throws IOException {
        buffer.getInt(); // block length = 1
        int level = buffer.get() & 0xFF;
        listener.onSetSignalLevel(level);
    }

    /**
     * Block 0x30 - Text Description.
     */
    private static void parseBlock30(ByteBuffer buffer, TapeListener listener) throws IOException {
        int len = buffer.get() & 0xFF;
        byte[] text = TapeReader.readBytes(buffer, len);
        listener.onTextDescription(new String(text, StandardCharsets.US_ASCII));
    }

    /**
     * Block 0x31 - Message Block.
     */
    private static void parseBlock31(ByteBuffer buffer, TapeListener listener) throws IOException {
        int displayTime = buffer.get() & 0xFF;
        int len = buffer.get() & 0xFF;
        byte[] msg = TapeReader.readBytes(buffer, len);
        listener.onMessage(new String(msg, StandardCharsets.US_ASCII), displayTime);
    }

    /**
     * Block 0x32 - Archive Info.
     */
    private static void parseBlock32(ByteBuffer buffer, TapeListener listener) throws IOException {
        buffer.getShort(); // block length
        int numStrings = buffer.get() & 0xFF;
        List<String[]> entries = new ArrayList<>();
        for (int i = 0; i < numStrings; i++) {
            int type = buffer.get() & 0xFF;
            int len = buffer.get() & 0xFF;
            byte[] text = TapeReader.readBytes(buffer, len);
            String typeName = type < ARCHIVE_INFO_TYPES.length ? ARCHIVE_INFO_TYPES[type] : "Unknown (" + type + ")";
            entries.add(new String[]{typeName, new String(text, StandardCharsets.US_ASCII)});
        }
        listener.onArchiveInfo(entries);
    }

    /**
     * Block 0x33 - Hardware Type.
     */
    private static void parseBlock33(ByteBuffer buffer, TapeListener listener) throws IOException {
        int count = buffer.get() & 0xFF;
        List<int[]> entries = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int hwType = buffer.get() & 0xFF;
            int hwId = buffer.get() & 0xFF;
            int hwInfo = buffer.get() & 0xFF;
            entries.add(new int[]{hwType, hwId, hwInfo});
        }
        listener.onHardwareInfo(entries);
    }

    /**
     * Block 0x35 - Custom Info Block.
     */
    private static void parseBlock35(ByteBuffer buffer, TapeListener listener) throws IOException {
        byte[] id = TapeReader.readBytes(buffer, 16);
        int len = buffer.getInt();
        byte[] data = TapeReader.readBytes(buffer, len);
        listener.onCustomInfo(new String(id, StandardCharsets.US_ASCII).trim(), data);
    }

    /**
     * Block 0x5A - Glue Block.
     */
    private static void parseBlock5A(ByteBuffer buffer, TapeListener listener) throws IOException {
        buffer.position(buffer.position() + 9); // skip 9 bytes
        listener.onGlueBlock();
    }


}
