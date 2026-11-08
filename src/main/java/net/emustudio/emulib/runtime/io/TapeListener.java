/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.io;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Tape signal callbacks, decoded from TAP/TZX blocks by {@link FileLoader}.
 * <p>
 * Usage: {@code FileLoader.load(path, format, tapeListener)}. Pulse lengths are in Z80 T-states (3.5 MHz),
 * bits are sent from the most significant one. Blocks which cannot be played are reported by {@link #onInfo}.
 */
public interface TapeListener extends FileLoader.Listener {

    /**
     * Standard-speed ROM data block (TAP block or TZX 0x10): pilot tone (8063 pulses if flag &lt; 0x80, otherwise
     * 3223 pulses), sync pulses and the packet bits.
     *
     * @param packet flag, data and checksum
     */
    void onStandardData(byte[] packet);

    /**
     * Sequence of pulses (TZX 0x12 pure tone, 0x13 pulse sequence, pilot and sync pulses of 0x11 turbo data).
     *
     * @param pulseLengths pulse lengths in T-states
     */
    void onPulses(int[] pulseLengths);

    /**
     * Data without pilot and sync pulses (TZX 0x14, data of 0x11 turbo data). Each bit is two pulses.
     *
     * @param zeroBitPulseLen    length of a pulse of bit 0
     * @param oneBitPulseLen     length of a pulse of bit 1
     * @param usedBitsInLastByte number of bits used in the last byte (1-8)
     * @param data               data bytes
     */
    void onPureData(int zeroBitPulseLen, int oneBitPulseLen, int usedBitsInLastByte, byte[] data);

    /**
     * Direct recording (TZX 0x15): each bit is the signal level of one sample.
     *
     * @param tstatesPerSample   length of one sample
     * @param usedBitsInLastByte number of samples used in the last byte (1-8)
     * @param samples            samples
     */
    void onDirectRecording(int tstatesPerSample, int usedBitsInLastByte, byte[] samples);

    /**
     * Silence (TZX 0x20, or the pause after a TZX data block).
     *
     * @param durationMs pause in milliseconds; 0 means "stop the tape"
     */
    void onPause(int durationMs);

    /**
     * Sets signal level (TZX 0x2B).
     *
     * @param level 0 = low, otherwise high
     */
    void onSetSignalLevel(int level);

    /**
     * Human-readable information about the tape: Spectrum headers ("PROGRAM", "NUMBER ARRAY", "STRING ARRAY",
     * "MEMORY BLOCK"), TZX informational blocks ("TEXT", "MESSAGE", "ARCHIVE", "HARDWARE", "CUSTOM", "GROUP",
     * "GROUP END", "GLUE", "STOP 48K"), unsupported blocks ("CSW REC", "GEN DATA", "SELECT") and "ERROR".
     *
     * @param type    information type
     * @param details details
     */
    default void onInfo(String type, String details) {
    }

    /**
     * Decodes a tape block into the callbacks of this listener. TZX control-flow blocks are ignored here
     * (the loader follows them).
     *
     * @param block tape block
     * @throws IOException if the block is not a tape block, or is malformed
     */
    @Override
    default void onBlock(FileLoader.Block block) throws IOException {
        if (!block.getFormat().isTape()) throw new IOException("Tape playback requires a TAP or TZX file");
        ByteBuffer b = block.buffer();
        try {
            switch (block.getId()) {
                case 0x10:
                    byte[] packet = block.getData();
                    if (packet.length < 2) throw new IOException("Tape data requires flag and checksum");
                    block.getSpectrumHeader().ifPresent(h -> describeHeader(this, h));
                    if (!block.hasValidChecksum()) onInfo("ERROR", "Wrong checksum");
                    onStandardData(packet);
                    pauseAfter(this, block.getFormat() == FileLoader.Format.TZX ? b.getShort() & 0xFFFF : 0);
                    break;
                case 0x11: {
                    int pilot = u16(b), sync1 = u16(b), sync2 = u16(b), zero = u16(b), one = u16(b);
                    int[] pulses = new int[u16(b) + 2];
                    Arrays.fill(pulses, pilot);
                    pulses[pulses.length - 2] = sync1;
                    pulses[pulses.length - 1] = sync2;
                    int usedBits = b.get() & 0xFF;
                    int pause = u16(b);
                    onPulses(pulses);
                    onPureData(zero, one, usedBits, block.getData());
                    pauseAfter(this, pause);
                    break;
                }
                case 0x12: {
                    int length = u16(b);
                    int[] pulses = new int[u16(b)];
                    Arrays.fill(pulses, length);
                    onPulses(pulses);
                    break;
                }
                case 0x13: {
                    int[] pulses = new int[b.get() & 0xFF];
                    for (int i = 0; i < pulses.length; i++) pulses[i] = u16(b);
                    onPulses(pulses);
                    break;
                }
                case 0x14: {
                    int zero = u16(b), one = u16(b), usedBits = b.get() & 0xFF, pause = u16(b);
                    onPureData(zero, one, usedBits, FileLoader.readBytes(b, uint24(b)));
                    pauseAfter(this, pause);
                    break;
                }
                case 0x15: {
                    int tstatesPerSample = u16(b), pause = u16(b), usedBits = b.get() & 0xFF;
                    onDirectRecording(tstatesPerSample, usedBits, FileLoader.readBytes(b, uint24(b)));
                    pauseAfter(this, pause);
                    break;
                }
                case 0x18: {
                    b.getInt();
                    int pause = u16(b), rate = uint24(b), compression = b.get() & 0xFF;
                    onInfo("CSW REC", String.format("rate=%d Hz, compression=%d, pulses=%d, pause=%d ms (not supported)",
                            rate, compression, b.getInt() & 0xFFFFFFFFL, pause));
                    break;
                }
                case 0x19:
                    onInfo("GEN DATA", String.format("pause=%d ms, length=0x%04X (not supported)",
                            b.getShort(4) & 0xFFFF, b.getInt(0) - 2));
                    break;
                case 0x20: onPause(u16(b)); break;
                case 0x21: onInfo("GROUP", text(b, b.get() & 0xFF)); break;
                case 0x22: onInfo("GROUP END", ""); break;
                case 0x28: onInfo("SELECT", "not supported"); break;
                case 0x2A: onInfo("STOP 48K", "Stop if in 48K mode"); break;
                case 0x2B: onSetSignalLevel(b.get(4) & 0xFF); break;
                case 0x30: onInfo("TEXT", text(b, b.get() & 0xFF)); break;
                case 0x31: {
                    int seconds = b.get() & 0xFF;
                    onInfo("MESSAGE", text(b, b.get() & 0xFF) + " (" + seconds + "s)");
                    break;
                }
                case 0x32: {
                    String[] types = {"Title", "Publisher", "Author", "Publication year", "Language", "Type", "Price",
                            "Protection", "Origin", "Comment"};
                    b.getShort();
                    StringBuilder details = new StringBuilder();
                    for (int i = b.get() & 0xFF; i > 0; i--) {
                        int type = b.get() & 0xFF;
                        details.append(details.length() > 0 ? "; " : "")
                                .append(type < types.length ? types[type] : "Unknown (" + type + ")")
                                .append(": ").append(text(b, b.get() & 0xFF));
                    }
                    onInfo("ARCHIVE", details.toString());
                    break;
                }
                case 0x33: onInfo("HARDWARE", (b.get() & 0xFF) + " entries"); break;
                case 0x35: onInfo("CUSTOM", text(b, 16).trim() + " (" + b.getInt() + " bytes)"); break;
                case 0x5A: onInfo("GLUE", ""); break;
                default: break; // control flow is followed by the loader
            }
        } catch (BufferUnderflowException | IndexOutOfBoundsException e) {
            throw new IOException(String.format("Malformed tape block 0x%02X", block.getId()), e);
        }
    }

    private static void describeHeader(TapeListener listener, FileLoader.SpectrumHeader h) {
        String name = h.getFileName();
        char variable = (char) (h.getParameter1() >>> 8);
        switch (h.getType()) {
            case 0: listener.onInfo("PROGRAM", name + " (start=" + h.getParameter1() + ", length=" + h.getParameter2() + ")"); break;
            case 1: listener.onInfo("NUMBER ARRAY", name + " (variable=" + variable + ")"); break;
            case 2: listener.onInfo("STRING ARRAY", name + " (variable=" + variable + ")"); break;
            case 3: listener.onInfo("MEMORY BLOCK", name + " (start=" + h.getParameter1() + ")"); break;
            default: listener.onInfo("ERROR", "Unknown Spectrum header type " + h.getType());
        }
    }

    private static void pauseAfter(TapeListener listener, int pauseMs) {
        if (pauseMs > 0) listener.onPause(pauseMs);
    }

    private static int u16(ByteBuffer b) {
        return b.getShort() & 0xFFFF;
    }

    private static int uint24(ByteBuffer b) {
        int value = FileLoader.uint24(b, b.position());
        b.position(b.position() + 3);
        return value;
    }

    private static String text(ByteBuffer b, int count) throws IOException {
        return new String(FileLoader.readBytes(b, count), StandardCharsets.US_ASCII);
    }
}
