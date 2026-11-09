/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.io;

import net.emustudio.emulib.io.FileLoaderTest.Tape;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.emustudio.emulib.io.FileLoaderTest.*;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public class TapeListenerTest {
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @Test
    public void tapHeadersAreDescribedBeforeStandardData() throws IOException {
        byte[] corrupt = packet(255, 0x42);
        corrupt[corrupt.length - 1] ^= 1;
        Path path = file("all.tap", tap(header(0, 10, 5), header(1, 'a' << 8, 5), header(2, 'b' << 8, 5),
                header(3, 32768, 2), header(9, 0, 0), packet(255, 1, 2), corrupt));
        assertEquals(List.of("start",
                "info PROGRAM: name       (start=10, length=32768)", "data " + hex(header(0, 10, 5)),
                "info NUMBER ARRAY: name       (variable=a)", "data " + hex(header(1, 'a' << 8, 5)),
                "info STRING ARRAY: name       (variable=b)", "data " + hex(header(2, 'b' << 8, 5)),
                "info MEMORY BLOCK: name       (start=32768)", "data " + hex(header(3, 32768, 2)),
                "info ERROR: Unknown Spectrum header type 9", "data " + hex(header(9, 0, 0)),
                "data FF0102FC",
                "info ERROR: Wrong checksum", "data FF42BC",
                "end"), play(path, FileLoader.Format.TAP));
    }

    @Test
    public void everyTzxBlockIsDecoded() throws IOException {
        byte[] packet = packet(255, 7);
        Tape blocks = new Tape()
                .b(0x10).w(1000, packet.length).b(packet)
                .b(0x10).w(0, packet.length).b(packet)
                .b(0x11).w(2168, 667, 735, 855, 1710, 3).b(6).w(500).t(packet.length).b(packet)
                .b(0x12).w(2168, 2)
                .b(0x13).b(2).w(667, 735)
                .b(0x14).w(855, 1710).b(7).w(0).t(2).b(0xAA, 0xBB)
                .b(0x15).w(79, 100).b(4).t(1).b(0xF0)
                .b(0x18).d(10).w(5).t(44100).b(1).d(99)
                .b(0x19).d(14).w(20).b(new byte[12])
                .b(0x20).w(300).b(0x20).w(0)
                .b(0x21).b(4).s("Game").b(0x22)
                .b(0x23).w(1).b(0x24).w(1).b(0x25)
                .b(0x26).w(0).b(0x27)
                .b(0x28).w(1).b(0)
                .b(0x2A).d(0)
                .b(0x2B).d(1).b(1)
                .b(0x30).b(5).s("Hello")
                .b(0x31).b(3).b(2).s("Hi")
                .b(0x32).w(13).b(2).b(0, 2).s("Ti").b(2, 6).s("Author")
                .b(0x33).b(1).b(0, 1, 0)
                .b(0x35).s(String.format("%-16s", "POKEs")).d(2).b(1, 2)
                .b(0x5A).b("XTape!".getBytes()).b(0x1A, 1, 20);
        assertEquals(List.of("start",
                "data FF07F8", "pause 1000",
                "data FF07F8",
                "pulses " + Arrays.toString(new int[]{2168, 2168, 2168, 667, 735}),
                "pure 855/1710 bits=6 FF07F8", "pause 500",
                "pulses [2168, 2168]",
                "pulses [667, 735]",
                "pure 855/1710 bits=7 AABB",
                "direct 79 bits=4 F0", "pause 100",
                "info CSW REC: rate=44100 Hz, compression=1, pulses=99, pause=5 ms (not supported)",
                "info GEN DATA: pause=20 ms, length=0x000C (not supported)",
                "pause 300", "pause 0",
                "info GROUP: Game", "info GROUP END: ",
                "info SELECT: not supported",
                "info STOP 48K: Stop if in 48K mode",
                "level 1",
                "info TEXT: Hello",
                "info MESSAGE: Hi (3s)",
                "info ARCHIVE: Title: Ti; Author: Author",
                "info HARDWARE: 1 entries",
                "info CUSTOM: POKEs (2 bytes)",
                "info GLUE: ",
                "end"), play(file("all.tzx", tzx(blocks)), FileLoader.Format.TZX));
    }

    @Test
    public void malformedOrNonTapeBlocksFail() throws IOException {
        assertThrows(IOException.class, () -> play(file("x.bin", new byte[]{1}), FileLoader.Format.BIN));
        assertThrows(IOException.class, () -> play(file("short.tzx", tzx(new Tape().b(0x10).w(0, 1).b(0))),
                FileLoader.Format.TZX));
        assertThrows(IOException.class, () -> play(file("archive.tzx", tzx(new Tape().b(0x32).w(1).b(1))),
                FileLoader.Format.TZX));
    }

    @Test
    public void timingTestsTapeProducesHeaderDataAndTurboBlocks() throws IOException {
        Path tape = Path.of("..", "..", "..", "timing_tests-48k_v1.0.tzx");
        assumeTrue("Real tape is not available", Files.exists(tape));
        List<String> events = play(tape, FileLoader.Format.TZX);
        events.removeIf(e -> e.startsWith("pause"));
        assertEquals(10, events.size());
        assertTrue(events.get(1).startsWith("info PROGRAM: "));
        assertTrue(events.get(2).startsWith("data 00"));
        assertTrue(events.get(3).startsWith("data FF"));
        for (int i = 0; i < 3; i++) {
            assertEquals("pulses", events.get(4 + 2 * i).split(" ")[0]);
            assertTrue(events.get(5 + 2 * i).startsWith("pure 855/1710 bits=8 "));
        }
    }

    private List<String> play(Path path, FileLoader.Format format) throws IOException {
        List<String> events = new ArrayList<>();
        FileLoader.load(path, format, new TapeListener() {
            @Override
            public void onFileStart() {
                events.add("start");
            }

            @Override
            public void onStandardData(byte[] packet) {
                events.add("data " + hex(packet));
            }

            @Override
            public void onPulses(int[] pulseLengths) {
                events.add("pulses " + Arrays.toString(pulseLengths));
            }

            @Override
            public void onPureData(int zero, int one, int usedBits, byte[] data) {
                events.add("pure " + zero + "/" + one + " bits=" + usedBits + " " + hex(data));
            }

            @Override
            public void onDirectRecording(int tstatesPerSample, int usedBits, byte[] samples) {
                events.add("direct " + tstatesPerSample + " bits=" + usedBits + " " + hex(samples));
            }

            @Override
            public void onPause(int durationMs) {
                events.add("pause " + durationMs);
            }

            @Override
            public void onSetSignalLevel(int level) {
                events.add("level " + level);
            }

            @Override
            public void onInfo(String type, String details) {
                events.add("info " + type + ": " + details);
            }

            @Override
            public void onFileEnd() {
                events.add("end");
            }
        });
        return events;
    }

    private Path file(String name, byte[] content) throws IOException {
        return Files.write(tmp.newFile(name).toPath(), content);
    }
}
