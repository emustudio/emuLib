/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.io;

import net.emustudio.emulib.plugins.memory.MemoryContext;
import net.jcip.annotations.NotThreadSafe;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * Generator and loader of 16-bit Intel Hex files (I8HEX).
 * <p>
 * File format is described here: <a href="https://en.wikipedia.org/wiki/Intel_HEX">Intel HEX</a>
 */
@NotThreadSafe
public class IntelHEX {

    // Number of data bytes emitted per data record (standard I8HEX record length)
    private static final int MAX_DATA_BYTES_COUNT_IN_LINE = 16;
    private static final String HEX_DIGITS = "0123456789ABCDEF";

    private final NavigableMap<Integer, Byte> program = new TreeMap<>();
    private int nextAddress;

    /**
     * Constructs new Intel HEX generator/loader.
     */
    public IntelHEX() {
    }

    /**
     * Adds sequential bytes into the code table.
     * The bytes are encoded as hex String, each byte consuming exactly 2 hex digits.
     * <p>
     * For example, <code>0A0B10</code> represents 3 bytes: <code>0x0A</code>, <code>0x0B</code> and <code>0x10</code>.
     *
     * @param hexData Hexadecimal representation of binary code
     * @return next address
     */
    public int add(String hexData) {
        if (hexData.isEmpty()) {
            return nextAddress;
        }
        for (int i = 0; i < hexData.length() - 1; i += 2) {
            program.put(nextAddress++, (byte) readByte(hexData, i));
        }
        return nextAddress;
    }

    /**
     * Adds sequential bytes into the code table.
     * <p>
     * The bytes are added in the same order as they appear (i.e. they are not converted to different endian).
     *
     * @param data sequential bytes to add
     * @return next address
     */
    public int add(byte... data) {
        for (byte d : data) {
            program.put(nextAddress++, d);
        }
        return nextAddress;
    }

    /**
     * Add code byte into the code table.
     *
     * @param data byte to add
     * @return next address
     */
    public int add(byte data) {
        program.put(nextAddress++, data);
        return nextAddress;
    }

    /**
     * Adds code (encoded as hex String) into the code table.
     * The code is added in the order from lowest to highest address found in the map.
     * <p>
     * Keys of the map represent addresses and values represent code.
     *
     * @param hexCodeMap sub-table with addresses and codes
     */
    public void add(Map<Integer, String> hexCodeMap) {
        int previousAddress = nextAddress;
        for (Map.Entry<Integer, String> entry : new TreeMap<>(hexCodeMap).entrySet()) {
            nextAddress = entry.getKey();
            add(entry.getValue());
        }
        nextAddress = Math.max(nextAddress, previousAddress);
    }

    /**
     * Get the program hex code, encoded as map of addresses/bytes.
     *
     * @return mutable map of program bytes, iterated in ascending address order
     */
    public Map<Integer, Byte> getCode() {
        return this.program;
    }

    /**
     * Set the next address where the next value will be assigned.
     *
     * @param address next address
     */
    public void setNextAddress(int address) {
        nextAddress = address;
    }

    /**
     * Method is similar to generateHex() method in that way, that compiled
     * program is also transformed into chunk of bytes, but not to hex file but
     * to the operating memory.
     *
     * @param <T>     Specific memory type
     * @param mem     context of operating memory
     * @param convert conversion of byte to T
     */
    public <T extends Number> void loadIntoMemory(MemoryContext<T> mem, Function<Byte, T> convert) {
        program.forEach((address, code) -> mem.write(address, convert.apply(code)));
    }

    /**
     * Generates a Intel Hex file based on the cached program map.
     *
     * @param outputPath output file
     * @throws java.io.IOException if the HEX file could not be written
     */
    public void generate(Path outputPath) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.US_ASCII)) {
            generate(writer);
        }
    }

    /**
     * Generates a Intel Hex file based on the cached program map.
     *
     * @param writer Writer used to store the Intel HEX content
     * @throws java.io.IOException if the HEX file could not be written
     */
    public void generate(Writer writer) throws IOException {
        int recordAddress = 0;
        int nextRecordAddress = 0;
        StringBuilder data = new StringBuilder(MAX_DATA_BYTES_COUNT_IN_LINE * 2);
        for (Map.Entry<Integer, Byte> entry : program.entrySet()) {
            int address = entry.getKey();
            if (data.length() > 0 && (address != nextRecordAddress || data.length() == MAX_DATA_BYTES_COUNT_IN_LINE * 2)) {
                writeRecord(writer, recordAddress, data);
                data.setLength(0);
            }
            if (data.length() == 0) recordAddress = address;
            appendByte(data, entry.getValue());
            nextRecordAddress = address + 1;
        }
        if (data.length() > 0) writeRecord(writer, recordAddress, data);
        writer.write(":00000001FF\n");
    }

    /**
     * Finds program location in memory.
     * <p>
     * It is the lowest address in the program, or zero if the program is empty.
     *
     * @return program memory location
     */
    public int findProgramLocation() {
        return program.isEmpty() ? 0 : program.firstKey();
    }

    /**
     * Parses Intel HEX file.
     * <p>
     * Blank lines and lines starting with ; after leading whitespace are ignored.
     * Input checksums are ignored. Malformed data records report their line number.
     *
     * @param file file
     * @return new instance of IntelHEX
     * @throws IOException if the file cannot be parsed
     */
    public static IntelHEX parse(File file) throws IOException {
        IntelHEX hexFile = new IntelHEX();

        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.ISO_8859_1)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.stripLeading();
                if (line.isEmpty() || line.startsWith(";")) continue;
                try {
                    if (line.charAt(0) != ':') {
                        throw new IOException("Expected ':' at Intel HEX line " + lineNumber);
                    }
                    int bytesCount = readByte(line, 1);
                    // Preserve the existing handling of EOF and other empty records.
                    if (bytesCount == 0) continue;
                    int address = Integer.parseInt(line, 3, 7, 16);
                    int dataType = readByte(line, 7);
                    if (dataType != 0) {
                        throw new IOException("Unsupported data type " + dataType + " at Intel HEX line " + lineNumber);
                    }
                    int dataEnd = 9 + bytesCount * 2;
                    if (line.length() < dataEnd) {
                        throw new IOException("Incomplete data at Intel HEX line " + lineNumber);
                    }
                    hexFile.setNextAddress(address);
                    for (int offset = 9; offset < dataEnd; offset += 2) {
                        hexFile.add((byte) readByte(line, offset));
                    }
                    // Input checksums remain ignored, as in the existing I8HEX reader.
                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                    throw new IOException("Malformed Intel HEX at line " + lineNumber, e);
                }
            }
        }
        return hexFile;
    }

    /**
     * Parse Intel HEX file and load it into memory.
     *
     * @param <T>     memory cell type
     * @param file    file
     * @param memory  target memory
     * @param convert conversion of byte to T
     * @return program start address
     * @throws IOException if the file cannot be parsed
     */
    public static <T extends Number> int loadIntoMemory(File file, MemoryContext<T> memory, Function<Byte, T> convert) throws IOException {
        IntelHEX hexFile = IntelHEX.parse(file);
        hexFile.loadIntoMemory(memory, convert);
        return hexFile.findProgramLocation();
    }

    private static void writeRecord(Writer writer, int address, StringBuilder data) throws IOException {
        String record = String.format("%02X%04X00%s", data.length() / 2, address, data);
        int sum = 0;
        for (int offset = 0; offset < record.length() - 1; offset += 2) {
            sum += readByte(record, offset);
        }
        writer.write(String.format(":%s%02X\n", record, (-sum) & 0xFF));
    }

    private static void appendByte(StringBuilder target, byte value) {
        target.append(HEX_DIGITS.charAt((value & 0xFF) >>> 4));
        target.append(HEX_DIGITS.charAt(value & 0x0F));
    }

    private static int readByte(String value, int offset) {
        return Integer.parseInt(value, offset, offset + 2, 16);
    }
}
