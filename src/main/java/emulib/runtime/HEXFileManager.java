/*
 * Copyright (C) 2007-2016, Peter Jakubčo
 * KISS, YAGNI, DRY
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

import emulib.plugins.memory.MemoryContext;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class generates and loads 16 bit Intel hex files.
 */
public class HEXFileManager {
    private final Map<Integer, String> program;
    private int nextAddress;

    public HEXFileManager() {
        this.program = new HashMap<>();
        nextAddress = 0;
    }

    /**
     * Put a series of bytes to the code table.
     *
     * The bytes must be given as a hexadecimal string of any length.
     * Each byte must have a form of two characters. For example:
     *
     * <code>0A0B10</code>
     *
     * represents 3 bytes: <code>0x0A</code>, <code>0x0B</code> and <code>0x10</code>.
     *
     * The code table is modified so that all addresses starting from the current
     * one up to the code length will contain corresponding byte.
     *
     * The current address is then increased by the code length.
     * If a byte exists on an address already, it is overwritten.
     *
     * @param code Hexadecimal representation of binary code
     * @return updated current address
     */
    public int putCode(String code) {
        if (code.isEmpty()) {
            return nextAddress;
        }
        int addr = nextAddress;
        for (int i = 0; i < code.length()-1; i += 2) {
            String tmp = code.substring(i, i + 2);
            program.put(addr, tmp);
            addr++;
        }
        nextAddress += (code.length() / 2);
        return nextAddress;
    }

    /**
     * Set the next address where the next value will be assigned.
     *
     * @param address
     */
    public void setNextAddress(int address) {
        nextAddress = address;
    }

    private String checksum(String lin) {
        int sum = 0, chsum;
        for (int i = 0; i < lin.length() - 1; i += 2) {
            sum += Integer.parseInt(lin.substring(i, i + 2), 16);
        }
        sum %= 0x100;
        // :
        // 10 00 08 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
        // 16 0  8  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0
        // 16+8 = 24
        // 0x100 -24 +1 = 256 - 24 +1 = 232 +1 = 0xe8 + 1 = 0xe9
        // 0xe9 je zevraj zle, ma byt 0xe8
        chsum = 0x100 - sum; //+1;
        return String.format("%1$02X", chsum);
    }

    /**
     * Keys of the HashMap have to represent adresses and values have to
     * represent compiled code. Method copies all elements from param HashMap to
     * internal data member.
     *
     * @param ha sub-table with addresses and codes
     */
    public void addTable(Map<Integer, String> ha) {
        List<Integer> adrs = new ArrayList<>(ha.keySet());
        int largestAdr = nextAddress;
        for (Integer adr : adrs) {
            nextAddress = adr;
            String cd = ha.get(nextAddress);
            putCode(cd);
            if (nextAddress > largestAdr) {
                largestAdr = nextAddress;
            }
        }
        nextAddress = largestAdr;
    }

    /**
     * Get the Map representing the program hex code.
     *
     * @return Map representing the program hex code
     */
    public Map<Integer, String> getTable() {
        return this.program;
    }

    // generate hex file
    private String generateHEX() {
        String lines = "";       // all lines
        String lineAddress = ""; // starting line address
        String line = "";        // line data
        int address = 0;         // current address in hex file
        int bytesCount = 0;      // current count of data bytes on single line

        List<Integer> adrs = new ArrayList<>(program.keySet());
        Collections.sort(adrs);

        // for all code elements (they won't be separated)
        for (Integer adr : adrs) {
            // is line very beginning ?
            if (lineAddress.isEmpty()) {
                address = adr;
                lineAddress = String.format("%1$04X", address);
            }

            // if element's address do not equal suggested (natural computed)
            // address or line is full
            if ((address != adr) || (bytesCount >= 16)) {
                String lin = String.format("%1$02X", bytesCount) + lineAddress
                        + "00" + line;
                lines += ":" + lin + checksum(lin) + "\n";
                bytesCount = 0;
                line = "";
                address = adr;
                lineAddress = String.format("%1$04X", address);
            }

            // code have to be stored as number of separate pairs of hex digits
            String cd = program.get(adr);
            line += cd;
            address += (cd.length() / 2); // compute next address
            bytesCount += (cd.length() / 2);
        }
        if (!line.isEmpty()) {
            String lin = String.format("%1$02X", bytesCount) + lineAddress
                    + "00" + line;
            lines += ":" + lin + checksum(lin) + "\n";
        }
        lines += ":00000001FF\n";
        return lines;
    }

    /**
     * Method is similar to generateHex() method in that way, that compiled
     * program is also transformed into chunk of bytes, but not to hex file but
     * to the operating memory.
     *
     * @param mem context of operating memory
     * @return true if the hex file was successfully loaded, false otherwise
     */
    public boolean loadIntoMemory(MemoryContext<Short> mem) {
        List<Integer> adrs = new ArrayList<>(program.keySet());
        Collections.sort(adrs);
        for (Integer adr : adrs) {
            String code = program.get(adr);
            for (int i = 0, j = 0; i < code.length() - 1; i += 2, j++) {
                String hexCode = code.substring(i, i + 2);
                short num = (short) ((Short.decode("0x" + hexCode)) & 0xFF);
                mem.write(adr + j, num);
            }
        }
        return true;
    }

    /**
     * Generates a Intel Hex file based on the cached program map.
     *
     * @param filename file name where to store the hex file
     * @throws java.io.IOException
     */
    public void generateFile(String filename) throws java.io.IOException {
        String fileData = generateHEX();
        try (BufferedWriter out = new BufferedWriter(new FileWriter(filename))) {
            out.write(fileData);
        }
    }

    /**
     * Get the program starting address (the first address that has occured in
     * the program HashMap).
     *
     * @return program starting memory location
     */
    public int getProgramStart() {
        List<Integer> adrs = new ArrayList<>(program.keySet());
        Collections.sort(adrs);
        if (!adrs.isEmpty()) {
            return adrs.get(0);
        } else {
            return 0;
        }
    }

    private static char ignoreSpaces(Reader reader) throws IOException {
        int input = reader.read();
        while (input == ' ') {
            input = reader.read();
        }
        return (char) input;
    }

    private static char ignoreLine(Reader reader) throws IOException {
        int input = reader.read();
        while (input != -1 && input != '\n') {
            input = reader.read();
        }
        return (char) input;
    }

    private static int readWord(Reader reader) throws Exception {
        int input = reader.read();
        if (input == -1) {
            reader.close();
            throw new Exception("Unexpected end of input");
        }
        char wordHigh = (char) input;
        input = reader.read();
        if (input == -1) {
            reader.close();
            throw new Exception("Unexpected end of input");
        }
        char wordLow = (char) input;
        return Integer.decode(String.format("0x%c%c", wordHigh, wordLow));
    }

    private static int readDword(Reader reader) throws Exception {
        int input = reader.read();
        if (input == -1) {
            reader.close();
            throw new Exception("Unexpected end of input");
        }
        char dwordHigh = (char) input;
        input = reader.read();
        if (input == -1) {
            reader.close();
            throw new Exception("Unexpected end of input");
        }
        char dwordLow = (char) input;
        input = reader.read();
        if (input == -1) {
            reader.close();
            throw new Exception("Unexpected end of input");
        }
        char wordHigh = (char) input;
        input = reader.read();
        if (input == -1) {
            reader.close();
            throw new Exception("Unexpected end of input");
        }
        char wordLow = (char) input;
        return Integer.decode(String.format("0x%c%c%c%c", dwordHigh, dwordLow, wordHigh, wordLow));
    }

    // line beginning with ; is ignored
    public static HEXFileManager parseFromFile(File file) throws Exception {
        HEXFileManager hexFile = new HEXFileManager();

        try (FileReader reader = new FileReader(file)) {
            int input;
            while ((input = reader.read()) != -1) {
                if (input == ' ') {
                    input = ignoreSpaces(reader);
                }
                if (input == ';') {
                    ignoreLine(reader);
                    continue;
                }
                if (input != ':') {
                    reader.close();
                    throw new IOException("Unexpected character: " + input);
                }
                // data bytes count
                int bytesCount = readWord(reader);
                if (bytesCount == 0) {
                    // ignore line
                    ignoreLine(reader);
                    continue;
                }
                // address
                int address = readDword(reader);

                // data type
                int dataType = readWord(reader);
                if (dataType != 0) {
                    reader.close();
                    throw new IOException("Unexpected data type: " + dataType);
                } // doesnt support other data types

                // data...
                hexFile.setNextAddress(address);
                for (int y = 0; y < bytesCount; y++) {
                    char[] cbuf = new char[2];
                    reader.read(cbuf);
                    if (cbuf[0] == '\n' || cbuf[1] == '\n') {
                      throw new IOException("Unexpected EOL");
                    }
                    hexFile.putCode(new String(cbuf));
                }
                // checksum - dont care..
                ignoreLine(reader);
            }
        }
        return hexFile;
    }

    public static int loadIntoMemory(File file, MemoryContext<Short> memory) throws Exception {
        HEXFileManager hexFile = HEXFileManager.parseFromFile(file);
        hexFile.loadIntoMemory(memory);
        return hexFile.getProgramStart();
    }
}
