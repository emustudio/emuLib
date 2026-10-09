/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.io;

import net.emustudio.emulib.plugins.memory.MemoryContext;
import net.emustudio.emulib.plugins.memory.annotations.Annotations;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.Assert.*;

public class IntelHEXTest {
    @Rule public TemporaryFolder tmp = new TemporaryFolder();
    private static final String VALID_HEX_FILE = "valid.hex";
    private static final String INVALID_HEX_FILE = "invalid.hex";
    private static final String INVALID2_HEX_FILE = "invalid2.hex";
    private static final String INVALID3_HEX_FILE = "invalid3.hex";
    private static final String INVALID4_HEX_FILE = "invalid4.hex";
    private static final String INVALID5_HEX_FILE = "invalid5.hex";
    private static final String INVALID6_HEX_FILE = "invalid6.hex";
    private IntelHEX hexFile;

    @Before
    public void setUp() {
        hexFile = new IntelHEX();
    }

    public File toFile(String file) throws URISyntaxException {
        return new File(Objects.requireNonNull(getClass().getClassLoader().getResource(file)).toURI());
    }

    private List<String> readFileContent(File fileName) throws Exception {
        return Files.readAllLines(Paths.get(fileName.toURI()));
    }

    private List<String> generateReadAndDeleteHexFile() throws Exception {
        Path tmpName = Path.of("tmp" + System.currentTimeMillis());
        hexFile.generate(tmpName);
        try {
            return readFileContent(tmpName.toFile());
        } finally {
            tmpName.toFile().delete();
        }
    }

    private void assertHexLineIsValid(String line) {
        // verify starting char
        assertTrue(line.startsWith(":"));

        // verify data length
        int dataLength = Integer.decode("0x" + line.substring(1, 3));
        assertEquals(dataLength * 2, line.length() - 11);

        int sum = 0;
        for (int offset = 1; offset < line.length(); offset += 2) {
            sum += Integer.parseInt(line.substring(offset, offset + 2), 16);
        }
        assertEquals("Record checksum", 0, sum & 0xFF);
    }

    @Test
    public void testPutBigCode() {
        String code = "010203";
        hexFile.add(code);
        Map<Integer, Byte> codeTable = hexFile.getCode();

        assertEquals(1, (byte) codeTable.get(0));
        assertEquals(2, (byte) codeTable.get(1));
        assertEquals(3, (byte) codeTable.get(2));
    }

    @Test
    public void testOverwriteCode() {
        hexFile.add("01");
        hexFile.setNextAddress(0);
        hexFile.add("02");

        Map<Integer, Byte> codeTable = hexFile.getCode();
        assertEquals(2, (byte) codeTable.get(0));
    }

    @Test
    public void testProgramStartOnEmptyCode() {
        assertEquals(0, hexFile.findProgramLocation());
    }

    @Test
    public void testCorrectProgramStart() {
        hexFile.setNextAddress(5);
        hexFile.add("010203");
        assertEquals(5, hexFile.findProgramLocation());
    }

    @Test
    public void testEmptyHex() throws Exception {
        List<String> content = generateReadAndDeleteHexFile();
        assertEquals(1, content.size());
        assertEquals(":00000001FF", content.get(0));
    }

    @Test
    public void testValidHex() throws Exception {
        hexFile.add("0102030405060708090A0B0C0D0E0F101112131415161718191A");
        List<String> content = generateReadAndDeleteHexFile();
        assertEquals(3, content.size());
        content.forEach(this::assertHexLineIsValid);
    }

    @Test
    public void testHighBytesAreParsedFromHexString() {
        hexFile.add("80FF00C0");
        Map<Integer, Byte> codeTable = hexFile.getCode();
        assertEquals((byte) 0x80, (byte) codeTable.get(0));
        assertEquals((byte) 0xFF, (byte) codeTable.get(1));
        assertEquals((byte) 0x00, (byte) codeTable.get(2));
        assertEquals((byte) 0xC0, (byte) codeTable.get(3));
    }

    @Test
    public void testHighBytesRoundTripThroughGenerateAndParse() throws Exception {
        hexFile.add(new byte[]{(byte) 0x80, (byte) 0xFF, (byte) 0xAB, (byte) 0xCD});
        Path tmpName = Path.of("tmp" + System.currentTimeMillis());
        hexFile.generate(tmpName);
        try {
            IntelHEX parsed = IntelHEX.parse(tmpName.toFile());
            Map<Integer, Byte> codeTable = parsed.getCode();
            assertEquals((byte) 0x80, (byte) codeTable.get(0));
            assertEquals((byte) 0xFF, (byte) codeTable.get(1));
            assertEquals((byte) 0xAB, (byte) codeTable.get(2));
            assertEquals((byte) 0xCD, (byte) codeTable.get(3));
        } finally {
            tmpName.toFile().delete();
        }
    }

    @Test
    public void testChecksumIsAlwaysTwoHexDigits() throws Exception {
        // A single 0xFF byte at address 0 makes the byte-sum a multiple of 0x100,
        // which previously produced a malformed 3-character checksum ("100").
        hexFile.add((byte) 0xFF);
        List<String> content = generateReadAndDeleteHexFile();
        content.forEach(this::assertHexLineIsValid);
    }

    @Test
    public void testPutEmptyCode() throws Exception {
        hexFile.add("");
        List<String> content = generateReadAndDeleteHexFile();
        assertEquals(1, content.size());
        assertEquals(":00000001FF", content.get(0));
    }

    private List<String> preprocessContent(List<String> hexContent) {
        List<String> content = new ArrayList<>();
        for (String line : hexContent) {
            line = line.trim();
            if (!line.startsWith(";")) {
                content.add(line);
            }
        }
        return content;
    }

    @Test
    public void testParsingValidHexFile() throws Exception {
        List<String> expected = preprocessContent(readFileContent(toFile(VALID_HEX_FILE)));

        hexFile = IntelHEX.parse(toFile(VALID_HEX_FILE));
        List<String> content = generateReadAndDeleteHexFile();
        assertArrayEquals(expected.toArray(), content.toArray());
    }

    @Test(expected = IOException.class)
    public void testParsingInvalidHexFile() throws Exception {
        hexFile = IntelHEX.parse(toFile(INVALID_HEX_FILE));
    }

    @Test(expected = IOException.class)
    public void testParsingInvalid2HexFile() throws Exception {
        hexFile = IntelHEX.parse(toFile(INVALID2_HEX_FILE));
    }

    @Test(expected = IOException.class)
    public void testParsingInvalid3HexFile() throws Exception {
        hexFile = IntelHEX.parse(toFile(INVALID3_HEX_FILE));
    }

    @Test(expected = IOException.class)
    public void testParsingInvalid4HexFile() throws Exception {
        hexFile = IntelHEX.parse(toFile(INVALID4_HEX_FILE));
    }

    @Test(expected = IOException.class)
    public void testParsingInvalid5HexFile() throws Exception {
        hexFile = IntelHEX.parse(toFile(INVALID5_HEX_FILE));
    }

    @Test(expected = IOException.class)
    public void testParsingInvalid6HexFile() throws Exception {
        hexFile = IntelHEX.parse(toFile(INVALID6_HEX_FILE));
    }

    @Test
    public void testAddTable() {
        Map<Integer, String> codeTable = new HashMap<>();
        codeTable.put(1, "010203");
        hexFile.add(codeTable);

        assertEquals(1, hexFile.findProgramLocation());
        Map<Integer, Byte> hexCodeTable = hexFile.getCode();

        assertEquals(1, (byte) hexCodeTable.get(1));
        assertEquals(2, (byte) hexCodeTable.get(2));
        assertEquals(3, (byte) hexCodeTable.get(3));
    }

    @Test
    public void testGenerationSplitsAtRecordLimitAndAddressGaps() throws IOException {
        hexFile.setNextAddress(0x40);
        hexFile.add((byte) 0xFF);
        hexFile.setNextAddress(0x10);
        for (byte value = 1; value <= 17; value++) hexFile.add(value);
        StringWriter writer = new StringWriter();
        hexFile.generate(writer);
        assertEquals(":100010000102030405060708090A0B0C0D0E0F1058\n"
                + ":0100200011CE\n:01004000FFC0\n:00000001FF\n", writer.toString());
    }

    @Test
    public void testParsingSortsAddressesAndKeepsLastOverlappingData() throws IOException {
        Path file = tmp.newFile("unordered.hex").toPath();
        Files.writeString(file, "\n  ; comment\r\n:02002000112200\r\n:02001000AABB00\r\n"
                + ":01001100CC00\r\n; final comment without newline");
        IntelHEX parsed = IntelHEX.parse(file.toFile());
        assertEquals(Map.of(0x10, (byte) 0xAA, 0x11, (byte) 0xCC,
                0x20, (byte) 0x11, 0x21, (byte) 0x22), parsed.getCode());
        assertEquals(List.of(0x10, 0x11, 0x20, 0x21), new ArrayList<>(parsed.getCode().keySet()));
        assertEquals(0x10, parsed.findProgramLocation());
    }

    @Test
    public void testParsingIgnoresBlankAndCommentOnlyLines() throws IOException {
        Path file = tmp.newFile("comments.hex").toPath();
        Files.writeString(file, "\n \n\t; comment\n; last comment");
        assertTrue(IntelHEX.parse(file.toFile()).getCode().isEmpty());
    }

    @Test
    public void testMalformedRecordReportsItsLineNumber() throws IOException {
        for (String record : List.of(":", ":01", ":02001000AA", ":01001000GG00", "01001000AA00")) {
            Path file = tmp.newFile().toPath();
            Files.writeString(file, "; comment\n\n" + record);
            IOException error = assertThrows(IOException.class, () -> IntelHEX.parse(file.toFile()));
            assertTrue(error.getMessage(), error.getMessage().contains("line 3"));
        }
    }

    @Test
    public void testAddTableResolvesOverlapsAndPreservesNextAddress() {
        hexFile.setNextAddress(0x100);
        hexFile.add((byte) 1);
        Map<Integer, String> table = new LinkedHashMap<>();
        table.put(0x20, "11");
        table.put(0x11, "FF");
        table.put(0x10, "010203");
        hexFile.add(table);
        assertEquals((byte) 0xFF, (byte) hexFile.getCode().get(0x11));
        assertEquals(0x102, hexFile.add((byte) 0x44));
        assertEquals((byte) 0x44, (byte) hexFile.getCode().get(0x101));
    }

    private static class MemoryContextStub implements MemoryContext<Short> {
        final ByteBuffer code = ByteBuffer.allocate(32);

        @Override
        public Short read(int location) {
            code.position(location);
            return (short) code.get();
        }

        @Override
        public Short[] read(int location, int count) {
            return new Short[0];
        }

        @Override
        public void write(int location, Short value) {
            code.position(location);
            code.put(value.byteValue());
        }

        @Override
        public void write(int location, Short[] values, int count) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Class<Short> getCellTypeClass() {
            return Short.class;
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addMemoryListener(MemoryListener listener) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void removeMemoryListener(MemoryListener listener) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public boolean areMemoryNotificationsEnabled() {
            return false;
        }

        @Override
        public Annotations annotations() {
            return null;
        }

        @Override
        public void setMemoryNotificationsEnabled(boolean enabled) {

        }
    }

    @Test
    public void testLoadIntoMemory() {
        MemoryContext<Short> mc = new MemoryContextStub();
        hexFile.setNextAddress(4);
        hexFile.add("010203");
        hexFile.loadIntoMemory(mc, Short::valueOf);
        assertEquals(3, (int) mc.read(6));
    }

    @Test
    public void testStaticLoadIntoMemory() throws Exception {
        hexFile = IntelHEX.parse(toFile(VALID_HEX_FILE));

        MemoryContext<Short> mc = new MemoryContextStub();
        int programStart = IntelHEX.loadIntoMemory(toFile(VALID_HEX_FILE), mc, Short::valueOf);
        assertEquals(hexFile.findProgramLocation(), programStart);

        Map<Integer, Byte> codeTable = hexFile.getCode();
        assertEquals((byte) codeTable.get(programStart), mc.read(programStart).byteValue());
    }
}
