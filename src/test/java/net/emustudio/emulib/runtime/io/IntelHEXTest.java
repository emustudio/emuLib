/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.io;

import net.emustudio.emulib.plugins.memory.MemoryContext;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class IntelHEXTest {
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();
    private final IntelHEX hex = new IntelHEX();

    @Test
    public void addOverwritesAndTracksProgramLocation() {
        assertEquals(0, hex.findProgramLocation());
        hex.setNextAddress(5);
        hex.add((byte) 0x80, (byte) 0xFF, (byte) 3);
        hex.setNextAddress(5);
        hex.add((byte) 2);
        assertEquals(Map.of(5, (byte) 2, 6, (byte) 0xFF, 7, (byte) 3), hex.getCode());
        assertEquals(5, hex.findProgramLocation());
        assertThrows(UnsupportedOperationException.class, () -> hex.getCode().clear());
    }

    @Test
    public void emptyProgramGeneratesOnlyEndOfFile() throws IOException {
        hex.add();
        assertEquals(List.of(":00000001FF"), generate());
    }

    @Test
    public void generationSplitsAtRecordLimitAndAddressGaps() throws IOException {
        hex.setNextAddress(0x40);
        hex.add((byte) 0xFF);
        hex.setNextAddress(0x10);
        for (byte value = 1; value <= 17; value++) hex.add(value);
        assertEquals(List.of(":100010000102030405060708090A0B0C0D0E0F1058", ":0100200011CE", ":01004000FFC0",
                ":00000001FF"), generate());
    }

    @Test
    public void generatedRecordsHaveValidChecksums() throws IOException {
        hex.add((byte) 0xFF); // byte-sum multiple of 0x100
        hex.add(new byte[26]);
        List<String> lines = generate();
        assertEquals(3, lines.size());
        for (String line : lines) {
            assertEquals(Integer.parseInt(line.substring(1, 3), 16) * 2, line.length() - 11);
            int sum = 0;
            for (int offset = 1; offset < line.length(); offset += 2) {
                sum += Integer.parseInt(line.substring(offset, offset + 2), 16);
            }
            assertEquals(0, sum & 0xFF);
        }
    }

    @Test
    public void generatedFileParsesBack() throws IOException {
        hex.setNextAddress(0x1234);
        hex.add((byte) 0x80, (byte) 0xFF, (byte) 0xAB, (byte) 0xCD);
        Path file = tmp.newFile().toPath();
        hex.generate(file);
        assertEquals(hex.getCode(), IntelHEX.parse(file).getCode());
    }

    @Test
    public void validFileRoundTrips() throws Exception {
        List<String> expected = Files.readAllLines(resource("valid.hex")).stream()
                .map(String::trim).filter(line -> !line.startsWith(";")).collect(Collectors.toList());
        IntelHEX parsed = IntelHEX.parse(resource("valid.hex"));
        Path file = tmp.newFile().toPath();
        parsed.generate(file);
        assertEquals(expected, Files.readAllLines(file));
    }

    @Test
    public void invalidFilesAreRejected() throws Exception {
        for (String name : List.of("invalid.hex", "invalid2.hex", "invalid3.hex", "invalid4.hex", "invalid5.hex",
                "invalid6.hex")) {
            Path file = resource(name);
            assertThrows(name, IOException.class, () -> IntelHEX.parse(file));
        }
    }

    @Test
    public void parsingSortsAddressesAndKeepsLastOverlappingData() throws IOException {
        Path file = tmp.newFile("unordered.hex").toPath();
        Files.writeString(file, "\n  ; comment\r\n:02002000112200\r\n:02001000AABB00\r\n"
                + ":01001100CC00\r\n; final comment without newline");
        IntelHEX parsed = IntelHEX.parse(file);
        assertEquals(Map.of(0x10, (byte) 0xAA, 0x11, (byte) 0xCC, 0x20, (byte) 0x11, 0x21, (byte) 0x22),
                parsed.getCode());
        assertEquals(List.of(0x10, 0x11, 0x20, 0x21), new ArrayList<>(parsed.getCode().keySet()));
        assertEquals(0x10, parsed.findProgramLocation());
    }

    @Test
    public void parsingIgnoresBlankAndCommentOnlyLines() throws IOException {
        Path file = tmp.newFile("comments.hex").toPath();
        Files.writeString(file, "\n \n\t; comment\n; last comment");
        assertTrue(IntelHEX.parse(file).getCode().isEmpty());
    }

    @Test
    public void malformedRecordReportsItsLineNumber() throws IOException {
        for (String record : List.of(":", ":01", ":02001000AA", ":01001000GG00", "01001000AA00", ":01001002AA00")) {
            Path file = tmp.newFile().toPath();
            Files.writeString(file, "; comment\n\n" + record);
            IOException error = assertThrows(IOException.class, () -> IntelHEX.parse(file));
            assertTrue(error.getMessage(), error.getMessage().contains("line 3"));
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void loadIntoMemoryWritesEveryByte() {
        MemoryContext<Byte> memory = createStrictMock(MemoryContext.class);
        memory.write(4, (byte) 1);
        memory.write(5, (byte) 2);
        memory.write(9, (byte) 3);
        replay(memory);
        hex.setNextAddress(4);
        hex.add((byte) 1, (byte) 2);
        hex.setNextAddress(9);
        hex.add((byte) 3);
        hex.loadIntoMemory(memory);
        verify(memory);
    }

    private List<String> generate() throws IOException {
        Path file = tmp.newFile().toPath();
        hex.generate(file);
        return Files.readAllLines(file);
    }

    private Path resource(String name) throws URISyntaxException {
        return Path.of(Objects.requireNonNull(getClass().getClassLoader().getResource(name)).toURI());
    }
}
