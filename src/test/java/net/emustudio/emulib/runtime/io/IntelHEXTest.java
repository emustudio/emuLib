/*
 * This file is part of emuLib.
 *
 * Copyright (C) 2006-2023  Peter Jakubčo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.emustudio.emulib.runtime.io;

import net.emustudio.emulib.plugins.memory.MemoryContext;
import net.emustudio.emulib.plugins.memory.annotations.Annotations;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.Assert.*;

public class IntelHEXTest {
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
        String tmpName = "tmp" + System.currentTimeMillis();
        hexFile.generate(tmpName);
        try {
            return readFileContent(new File(tmpName));
        } finally {
            new File(tmpName).delete();
        }
    }

    private void assertHexLineIsValid(String line) {
        // verify starting char
        assertTrue(line.startsWith(":"));

        // verify data length
        int dataLength = Integer.decode("0x" + line.substring(1, 3));
        assertEquals(dataLength * 2, line.length() - 11);
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

    @Test(expected = Exception.class)
    public void testParsingInvalidHexFile() throws Exception {
        hexFile = IntelHEX.parse(toFile(INVALID_HEX_FILE));
    }

    @Test(expected = Exception.class)
    public void testParsingInvalid2HexFile() throws Exception {
        hexFile = IntelHEX.parse(toFile(INVALID2_HEX_FILE));
    }

    @Test(expected = Exception.class)
    public void testParsingInvalid3HexFile() throws Exception {
        hexFile = IntelHEX.parse(toFile(INVALID3_HEX_FILE));
    }

    @Test(expected = Exception.class)
    public void testParsingInvalid4HexFile() throws Exception {
        hexFile = IntelHEX.parse(toFile(INVALID4_HEX_FILE));
    }

    @Test(expected = Exception.class)
    public void testParsingInvalid5HexFile() throws Exception {
        hexFile = IntelHEX.parse(toFile(INVALID5_HEX_FILE));
    }

    @Test(expected = Exception.class)
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
