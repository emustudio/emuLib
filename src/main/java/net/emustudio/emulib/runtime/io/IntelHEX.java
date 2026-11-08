/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.io;

import net.emustudio.emulib.plugins.memory.MemoryContext;
import net.jcip.annotations.NotThreadSafe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Program code of 16-bit Intel HEX files (I8HEX): built by compilers, written into a file, parsed or loaded into memory.
 * <p>
 * File format is described here: <a href="https://en.wikipedia.org/wiki/Intel_HEX">Intel HEX</a>
 */
@NotThreadSafe
public class IntelHEX {
    private static final int MAX_DATA_BYTES_IN_RECORD = 16;

    private final NavigableMap<Integer, Byte> program = new TreeMap<>();
    private int nextAddress;

    /**
     * Adds bytes at the next address, in the given order. Existing bytes are overwritten.
     *
     * @param data bytes to add
     */
    public void add(byte... data) {
        for (byte d : data) {
            program.put(nextAddress++, d);
        }
    }

    /**
     * Sets the address where the next byte will be added.
     *
     * @param address next address
     */
    public void setNextAddress(int address) {
        nextAddress = address;
    }

    /**
     * @return unmodifiable view of program bytes by address, in ascending address order
     */
    public NavigableMap<Integer, Byte> getCode() {
        return Collections.unmodifiableNavigableMap(program);
    }

    /**
     * @return the lowest address of the program, or 0 if the program is empty
     */
    public int findProgramLocation() {
        return program.isEmpty() ? 0 : program.firstKey();
    }

    /**
     * Writes the program into memory.
     *
     * @param memory target memory
     */
    public void loadIntoMemory(MemoryContext<Byte> memory) {
        program.forEach(memory::write);
    }

    /**
     * Writes the program into an Intel HEX file. Records hold up to 16 bytes and break at address gaps.
     *
     * @param outputPath output file
     * @throws IOException if the file could not be written
     */
    public void generate(Path outputPath) throws IOException {
        try (Writer writer = Files.newBufferedWriter(outputPath, StandardCharsets.US_ASCII)) {
            int recordAddress = 0;
            StringBuilder data = new StringBuilder();
            for (Map.Entry<Integer, Byte> entry : program.entrySet()) {
                int address = entry.getKey();
                int size = data.length() / 2;
                if (size > 0 && (address != recordAddress + size || size == MAX_DATA_BYTES_IN_RECORD)) {
                    writeRecord(writer, recordAddress, data);
                    data.setLength(0);
                }
                if (data.length() == 0) recordAddress = address;
                data.append(String.format("%02X", entry.getValue()));
            }
            if (data.length() > 0) writeRecord(writer, recordAddress, data);
            writer.write(":00000001FF\n");
        }
    }

    /**
     * Parses an Intel HEX file.
     * <p>
     * Blank lines and lines starting with ';' (after leading whitespace) are ignored, as are input checksums
     * and empty records. Only data records are supported. Later records overwrite earlier ones.
     *
     * @param file input file
     * @return parsed program
     * @throws IOException if the file cannot be read or is malformed (the message contains the line number)
     */
    public static IntelHEX parse(Path file) throws IOException {
        IntelHEX hex = new IntelHEX();
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.ISO_8859_1)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.stripLeading();
                if (line.isEmpty() || line.startsWith(";")) continue;
                try {
                    if (line.charAt(0) != ':') throw new IOException("Expected ':' at Intel HEX line " + lineNumber);
                    int count = readByte(line, 1);
                    if (count == 0) continue;
                    int type = readByte(line, 7);
                    if (type != 0) throw new IOException("Unsupported data type " + type + " at Intel HEX line " + lineNumber);
                    if (line.length() < 9 + count * 2) throw new IOException("Incomplete data at Intel HEX line " + lineNumber);
                    hex.setNextAddress(Integer.parseInt(line, 3, 7, 16));
                    for (int offset = 9; offset < 9 + count * 2; offset += 2) {
                        hex.add((byte) readByte(line, offset));
                    }
                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                    throw new IOException("Malformed Intel HEX at line " + lineNumber, e);
                }
            }
        }
        return hex;
    }

    private static void writeRecord(Writer writer, int address, StringBuilder data) throws IOException {
        String record = String.format("%02X%04X00%s", data.length() / 2, address, data);
        int sum = 0;
        for (int offset = 0; offset < record.length(); offset += 2) {
            sum += readByte(record, offset);
        }
        writer.write(String.format(":%s%02X\n", record, (-sum) & 0xFF));
    }

    private static int readByte(String value, int offset) {
        return Integer.parseInt(value, offset, offset + 2, 16);
    }
}
