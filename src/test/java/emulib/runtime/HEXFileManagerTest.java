/*
 * HEXFileManagerTest.java
 *
 * KISS, YAGNI, DRY
 *
 * (c) Copyright 2013, Peter Jakubčo
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package emulib.runtime;

import emulib.plugins.memory.Memory;
import emulib.plugins.memory.MemoryContext;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HEXFileManagerTest {
    private static final String VALID_HEX_FILE = "valid.hex";
    private static final String INVALID_HEX_FILE = "invalid.hex";
    private static final String INVALID2_HEX_FILE = "invalid2.hex";
    private static final String INVALID3_HEX_FILE = "invalid3.hex";
    private static final String INVALID4_HEX_FILE = "invalid4.hex";
    private static final String INVALID5_HEX_FILE = "invalid5.hex";
    private static final String INVALID6_HEX_FILE = "invalid6.hex";
    private HEXFileManager hexFile;
    
    @Before
    public void setUp() {
        hexFile = new HEXFileManager();
    }

    public File toFile(String file) throws URISyntaxException {
        return new File(getClass().getClassLoader().getResource(file).toURI());
    }

    private List<String> readFileContent(File fileName) throws Exception {
        return Files.readAllLines(Paths.get(fileName.toURI()));
    }
    
    private List<String> generateReadAndDeleteHexFile() throws Exception {
        String tmpName = "tmp" + System.currentTimeMillis();
        hexFile.generateFile(tmpName);
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
        hexFile.putCode(code);
        Map<Integer, String> codeTable = hexFile.getTable();
        
        assertEquals("01", codeTable.get(0));
        assertEquals("02", codeTable.get(1));
        assertEquals("03", codeTable.get(2));
    }
    
    @Test
    public void testOverwriteCode() {
        hexFile.putCode("01");
        hexFile.setNextAddress(0);
        hexFile.putCode("02");
        
        Map<Integer, String> codeTable = hexFile.getTable();
        assertEquals("02", codeTable.get(0));
    }
    
    @Test
    public void testProgramStartOnEmptyCode() {
        assertEquals(0, hexFile.getProgramStart());
    }
    
    @Test
    public void testCorrectProgramStart() {
        hexFile.setNextAddress(5);
        hexFile.putCode("010203");
        assertEquals(5, hexFile.getProgramStart());
    }

    @Test
    public void testEmptyHex() throws Exception {
        List<String> content = generateReadAndDeleteHexFile();
        assertEquals(1, content.size());
        assertEquals(":00000001FF", content.get(0));
    }
   
    @Test
    public void testValidHex() throws Exception {
        hexFile.putCode("0102030405060708090A0B0C0D0E0F101112131415161718191A");
        List<String> content = generateReadAndDeleteHexFile();
        assertEquals(3, content.size());
        for (String line : content) {
            assertHexLineIsValid(line);
        }
    }
    
    @Test
    public void testPutEmptyCode() throws Exception {
        hexFile.putCode("");
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

        hexFile = HEXFileManager.parseFromFile(toFile(VALID_HEX_FILE));
        List<String> content = generateReadAndDeleteHexFile();
        assertArrayEquals(expected.toArray(), content.toArray());
    }

    @Test(expected = Exception.class)
    public void testParsingInvalidHexFile() throws Exception {
        hexFile = HEXFileManager.parseFromFile(toFile(INVALID_HEX_FILE));
    }
    
    @Test(expected = Exception.class)
    public void testParsingInvalid2HexFile() throws Exception {
        hexFile = HEXFileManager.parseFromFile(toFile(INVALID2_HEX_FILE));
    }

    @Test(expected = Exception.class)
    public void testParsingInvalid3HexFile() throws Exception {
        hexFile = HEXFileManager.parseFromFile(toFile(INVALID3_HEX_FILE));
    }

    @Test(expected = Exception.class)
    public void testParsingInvalid4HexFile() throws Exception {
        hexFile = HEXFileManager.parseFromFile(toFile(INVALID4_HEX_FILE));
    }

    @Test(expected = Exception.class)
    public void testParsingInvalid5HexFile() throws Exception {
        hexFile = HEXFileManager.parseFromFile(toFile(INVALID5_HEX_FILE));
    }
    
    @Test(expected = Exception.class)
    public void testParsingInvalid6HexFile() throws Exception {
        hexFile = HEXFileManager.parseFromFile(toFile(INVALID6_HEX_FILE));
    }

    @Test
    public void testAddTable() {
        Map<Integer, String> codeTable = new HashMap<>();
        codeTable.put(1, "010203");
        hexFile.addTable(codeTable);
        
        assertEquals(1, hexFile.getProgramStart());
        codeTable = hexFile.getTable();
        
        assertEquals("01", codeTable.get(1));
        assertEquals("02", codeTable.get(2));
        assertEquals("03", codeTable.get(3));
    }
    
    private static class MemoryContextStub implements MemoryContext<Short, Integer> {
        Map<Integer, Short> code = new HashMap<>();
    
        @Override
        public Short read(int memoryPosition) {
            return code.get(memoryPosition);
        }

        @Override
        public Integer readWord(int memoryPosition) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void write(int memoryPosition, Short value) {
            code.put(memoryPosition, value);
        }

        @Override
        public void writeWord(int memoryPosition, Integer value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Class<?> getDataType() {
            return Short.class;
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addMemoryListener(Memory.MemoryListener listener) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void removeMemoryListener(Memory.MemoryListener listener) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getSize() {
            return 0;
        }
    }
    
    @Test
    public void testLoadIntoMemory() {
        MemoryContext<Short, Integer> mc = new MemoryContextStub();
        hexFile.setNextAddress(4);
        hexFile.putCode("010203");
        hexFile.loadIntoMemory(mc);
        assertEquals(3, (int)mc.read(6));
    }
    
    @Test
    public void testStaticLoadIntoMemory() throws Exception {
        hexFile = HEXFileManager.parseFromFile(toFile(VALID_HEX_FILE));

        MemoryContext<Short, Integer> mc = new MemoryContextStub();
        int programStart = HEXFileManager.loadIntoMemory(toFile(VALID_HEX_FILE), mc);
        assertEquals(hexFile.getProgramStart(), programStart);
        
        Map<Integer, String> codeTable = hexFile.getTable();
        assertEquals(Integer.decode("0x" + codeTable.get(programStart)).intValue(),
                (int)mc.read(programStart));
    }

}
