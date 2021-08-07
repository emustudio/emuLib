/*
 * This file is part of emuLib.
 *
 * Copyright (C) 2006-2020  Peter Jakubčo
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
import net.jcip.annotations.NotThreadSafe;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Generator and loader of 16-bit Intel Hex files (I8HEX).
 * <p>
 * File format is described here: https://en.wikipedia.org/wiki/Intel_HEX
 */
@NotThreadSafe
public class IntelHEX {
    // 16-bit Intel HEX has max 15 bytes per line
    private final static int MAX_DATA_BYTES_COUNT_IN_LINE = 15;

    private final Map<Integer, Byte> program = new HashMap<>();
    private int nextAddress;

    /**
     * Add a series of bytes, encoded as hex String, into the code table.
     * <p>
     * The bytes must be given as a hexadecimal string of any length.
     * Each byte must have a form of two characters. For example:
     *
     * <code>0A0B10</code>
     * <p>
     * represents 3 bytes: <code>0x0A</code>, <code>0x0B</code> and <code>0x10</code>.
     * <p>
     * The code table is modified so that all addresses starting from the current
     * one up to the code length will contain corresponding byte.
     * <p>
     * The current address is then increased by the code length.
     * If a byte exists on an address already, it is overwritten.
     *
     * @param hexString Hexadecimal representation of binary code
     * @return updated current address
     */
    public int add(String hexString) {
        if (hexString.isEmpty()) {
            return nextAddress;
        }
        for (int i = 0; i < hexString.length() - 1; i += 2) {
            String tmp = hexString.substring(i, i + 2);
            program.put(nextAddress++, Byte.parseByte(tmp, 16));
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
        hexCodeMap.keySet().stream().sorted().forEach(address -> {
            nextAddress = address;
            add(hexCodeMap.get(nextAddress));
        });
        nextAddress = Math.max(nextAddress, previousAddress);
    }

    /**
     * Get the program hex code, encoded as map of addresses/bytes.
     *
     * @return Map representing the program code
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
     * @param <T> Specific memory type
     * @param mem context of operating memory
     * @param convert conversion of byte to T
     */
    public <T extends Number> void loadIntoMemory(MemoryContext<T> mem, Function<Byte, T> convert) {
        program.keySet().stream().sorted().forEach(address -> {
            byte code = program.get(address);
            mem.write(address, convert.apply(code));
        });
    }

    /**
     * Generates a Intel Hex file based on the cached program map.
     *
     * @param outputFileName file name where to store the hex file
     * @throws java.io.IOException if the HEX file could not be written
     */
    public void generate(String outputFileName) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName))) {
            generate(writer);
        }
    }

    /**
     * Generates a Intel Hex file based on the cached program map.
     *
     * @param writer Writer used to store the Intel HEX content
     * @throws java.io.IOException if the HEX file could not be written
     */
    public void generate(Writer writer) throws java.io.IOException {
        String fileData = generateHEX();
        writer.write(fileData);
    }

    /**
     * Finds program location in memory.
     * <p>
     * It is actually the the first address which has occurred in the program.
     *
     * @return program memory location
     */
    public int findProgramLocation() {
        return program.keySet().stream().sorted().findFirst().orElse(0);
    }

    /**
     * Parses Intel HEX file.
     * <p>
     * Lines starting with ; are ignored.
     *
     * @param file file
     * @return new instance of IntelHEX
     * @throws Exception if the file cannot be parsed
     */
    public static IntelHEX parse(File file) throws Exception {
        IntelHEX hexFile = new IntelHEX();

        try (FileChannel channel = new FileInputStream(file).getChannel()) {
            ByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());

            while (buffer.hasRemaining()) {
                char input = (char) buffer.get();
                while (buffer.hasRemaining() && (input == ' ' || input == ';')) {
                    if (input == ' ') {
                        input = ignoreSpaces(buffer);
                    }
                    if (input == ';') {
                        ignoreLine(buffer);
                        input = (char) buffer.get();
                    }
                }
                if (!buffer.hasRemaining()) {
                    break;
                }
                if (input != ':') {
                    throw new IOException("Unexpected character: " + input);
                }

                // data bytes count
                int bytesCount = readWord(buffer);
                if (bytesCount == 0) {
                    ignoreLine(buffer);
                    continue;
                }

                // address
                int address = readDword(buffer);

                // data type
                int dataType = readWord(buffer);
                if (dataType != 0) {
                    throw new IOException("Unsupported data type: " + dataType);
                }

                // data...
                hexFile.setNextAddress(address);
                byte[] hex = new byte[2];
                for (int y = 0; y < bytesCount; y++) {
                    buffer.get(hex);
                    hexFile.add(Byte.parseByte(new String(hex), 16));
                }
                // checksum - don't care..
                ignoreLine(buffer);
            }
        }
        return hexFile;
    }

    /**
     * Parse Intel HEX file and load it into memory.
     *
     * @param <T> specific memory type
     * @param file   file
     * @param memory memory
     * @param convert conversion of byte to T
     * @return program start address
     * @throws Exception if the file cannot be parsed
     */
    public static <T extends Number> int loadIntoMemory(File file, MemoryContext<T> memory, Function<Byte, T> convert) throws Exception {
        IntelHEX hexFile = IntelHEX.parse(file);
        hexFile.loadIntoMemory(memory, convert);
        return hexFile.findProgramLocation();
    }

    // generate hex file
    private String generateHEX() {
        final AtomicReference<String> hexLineAddressStr = new AtomicReference<>();
        final AtomicInteger hexLineAddress = new AtomicInteger(0);
        final AtomicInteger hexDataBytesCount = new AtomicInteger(0);
        StringBuilder hexDataBytes = new StringBuilder();

        StringBuilder intelHexContent = new StringBuilder();

        program.keySet().stream().sorted().forEach(address -> {
            // is line at the very beginning ?
            if (hexLineAddressStr.get() == null) {
                hexLineAddress.set(address);
                hexLineAddressStr.set(String.format("%04X", address));
            }

            // if element's address does not equal suggested (naturally computed) address or line is full
            if ((hexLineAddress.get() != address) || (hexDataBytesCount.get() > MAX_DATA_BYTES_COUNT_IN_LINE)) {
                String fullLine = String.format("%02X%s00%s", hexDataBytesCount.get(), hexLineAddressStr.get(), hexDataBytes);
                intelHexContent.append(String.format(":%s%s\n", fullLine, checksum(fullLine)));
                hexDataBytesCount.set(0);
                hexDataBytes.setLength(0);
                hexLineAddress.set(address);
                hexLineAddressStr.set(String.format("%04X", address));
            }

            // code have to be stored as number of separate pairs of hex digits
            hexDataBytes.append(String.format("%02X", program.get(address)));
            hexLineAddress.incrementAndGet();
            hexDataBytesCount.incrementAndGet();
        });

        if (hexDataBytes.length() > 0) {
            String fullLine = String.format("%02X%s00%s", hexDataBytesCount.get(), hexLineAddressStr.get(), hexDataBytes);
            intelHexContent.append(String.format(":%s%s\n", fullLine, checksum(fullLine)));
        }
        intelHexContent.append(":00000001FF\n");
        return intelHexContent.toString();
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

    private static char ignoreSpaces(ByteBuffer buffer) {
        byte c = buffer.get();
        while (buffer.hasRemaining() && c == ' ') {
            c = buffer.get();
        }
        return (char)c;
    }

    private static void ignoreLine(ByteBuffer buffer) {
        if (buffer.hasRemaining()) {
            byte c = buffer.get();
            while (buffer.hasRemaining() && c != '\n') {
                c = buffer.get();
            }
        }
    }

    private static int readWord(ByteBuffer buffer) {
        return Integer.parseInt(String.format("%c%c", buffer.get(), buffer.get()), 16);
    }

    private static int readDword(ByteBuffer buffer) {
        String hexString = String.format("%c%c%c%c", buffer.get(), buffer.get(), buffer.get(), buffer.get());
        return Integer.parseInt(hexString, 16);
    }
}
