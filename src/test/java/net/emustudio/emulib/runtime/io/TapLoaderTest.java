/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.io;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.easymock.EasyMock.*;

public class TapLoaderTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    /**
     * Creates a TAP block.
     * TAP block format:
     * 2 bytes: block length (little-endian) = data.length + 2 (flag + checksum)
     * 1 byte: flag
     * N bytes: data
     * 1 byte: checksum (XOR of flag and all data bytes)
     */
    private byte[] createTapBlock(int flag, byte[] data) {
        int blockLength = data.length + 2; // flag + checksum
        ByteBuffer buf = ByteBuffer.allocate(2 + blockLength);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putShort((short) blockLength);
        buf.put((byte) flag);
        buf.put(data);

        // compute checksum: XOR flag with all data bytes
        int checksum = flag;
        for (byte d : data) {
            checksum ^= (d & 0xFF);
        }
        buf.put((byte) checksum);
        return buf.array();
    }

    /**
     * Creates a TAP header data block (17 bytes: 1 id + 10 filename + 2 dataLength + 2 param1 + 2 param2).
     */
    private byte[] createHeaderData(int id, String fileName, int dataLength, int param1, int param2) {
        ByteBuffer buf = ByteBuffer.allocate(17);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.put((byte) id);
        byte[] nameBytes = new byte[10];
        byte[] src = fileName.getBytes();
        System.arraycopy(src, 0, nameBytes, 0, Math.min(src.length, 10));
        // pad with spaces
        for (int i = src.length; i < 10; i++) {
            nameBytes[i] = ' ';
        }
        buf.put(nameBytes);
        buf.putShort((short) dataLength);
        buf.putShort((short) param1);
        buf.putShort((short) param2);
        return buf.array();
    }

    private File writeTapFile(byte[]... blocks) throws IOException {
        File file = tempFolder.newFile("test.tap");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            for (byte[] block : blocks) {
                fos.write(block);
            }
        }
        return file;
    }

    @Test
    public void testLoadHeaderAndDataBlock() throws IOException {
        byte[] headerData = createHeaderData(0, "TestProg", 100, 10, 50);
        byte[] headerBlock = createTapBlock(0x00, headerData); // flag < 0x80 => header

        byte[] data = new byte[]{0x01, 0x02, 0x03, 0x04};
        byte[] dataBlock = createTapBlock(0xFF, data); // flag >= 0x80 => data

        File tapFile = writeTapFile(headerBlock, dataBlock);

        TapeListener playback = createStrictMock(TapeListener.class);
        playback.onFileStart();
        // Header block
        playback.onHeaderStart();
        playback.onProgram(eq("TestProg  "), eq(100), eq(10), eq(50));
        playback.onBlockFlag(0x00);
        playback.onBlockData(aryEq(headerData));
        playback.onBlockChecksum(anyByte());
        // Data block
        playback.onDataStart();
        playback.onBlockFlag(0xFF);
        playback.onBlockData(aryEq(data));
        playback.onBlockChecksum(anyByte());
        playback.onFileEnd();
        replay(playback);

        FileLoader loader = FileLoader.forFormat(FileLoader.Format.TAP);
        java.nio.file.Path path = tapFile.toPath();
        loader.load(path, callbacks(playback), FileLoader.Options.playback());

        verify(playback);
    }

    @Test
    public void testLoadNumberArrayHeader() throws IOException {
        // id=1 is number array
        int variable = ('C' << 8);
        byte[] headerData = createHeaderData(1, "NumArray", 200, variable, 0);
        byte[] headerBlock = createTapBlock(0x00, headerData);

        File tapFile = writeTapFile(headerBlock);

        TapeListener playback = createStrictMock(TapeListener.class);
        playback.onFileStart();
        playback.onHeaderStart();
        playback.onNumberArray(eq("NumArray  "), eq(200), eq('C'));
        playback.onBlockFlag(0x00);
        playback.onBlockData(aryEq(headerData));
        playback.onBlockChecksum(anyByte());
        playback.onFileEnd();
        replay(playback);

        FileLoader loader = FileLoader.forFormat(FileLoader.Format.TAP);
        java.nio.file.Path path = tapFile.toPath();
        loader.load(path, callbacks(playback), FileLoader.Options.playback());

        verify(playback);
    }

    @Test
    public void testLoadStringArrayHeader() throws IOException {
        // id=2 is string array
        int variable = ('Z' << 8);
        byte[] headerData = createHeaderData(2, "StrArray", 300, variable, 0);
        byte[] headerBlock = createTapBlock(0x00, headerData);

        File tapFile = writeTapFile(headerBlock);

        TapeListener playback = createStrictMock(TapeListener.class);
        playback.onFileStart();
        playback.onHeaderStart();
        playback.onStringArray(eq("StrArray  "), eq(300), eq('Z'));
        playback.onBlockFlag(0x00);
        playback.onBlockData(aryEq(headerData));
        playback.onBlockChecksum(anyByte());
        playback.onFileEnd();
        replay(playback);

        FileLoader loader = FileLoader.forFormat(FileLoader.Format.TAP);
        java.nio.file.Path path = tapFile.toPath();
        loader.load(path, callbacks(playback), FileLoader.Options.playback());

        verify(playback);
    }

    @Test
    public void testLoadMemoryBlockHeader() throws IOException {
        // id=3 is memory block
        byte[] headerData = createHeaderData(3, "MemBlock", 512, 32768, 0);
        byte[] headerBlock = createTapBlock(0x00, headerData);

        File tapFile = writeTapFile(headerBlock);

        TapeListener playback = createStrictMock(TapeListener.class);
        playback.onFileStart();
        playback.onHeaderStart();
        playback.onMemoryBlock(eq("MemBlock  "), eq(512), eq(32768));
        playback.onBlockFlag(0x00);
        playback.onBlockData(aryEq(headerData));
        playback.onBlockChecksum(anyByte());
        playback.onFileEnd();
        replay(playback);

        FileLoader loader = FileLoader.forFormat(FileLoader.Format.TAP);
        java.nio.file.Path path = tapFile.toPath();
        loader.load(path, callbacks(playback), FileLoader.Options.playback());

        verify(playback);
    }

    @Test
    public void testLoadDataOnlyBlock() throws IOException {
        byte[] data = new byte[]{(byte) 0xAA, (byte) 0xBB, (byte) 0xCC};
        byte[] dataBlock = createTapBlock(0xFF, data);

        File tapFile = writeTapFile(dataBlock);

        TapeListener playback = createStrictMock(TapeListener.class);
        playback.onFileStart();
        playback.onDataStart();
        playback.onBlockFlag(0xFF);
        playback.onBlockData(aryEq(data));
        playback.onBlockChecksum(anyByte());
        playback.onFileEnd();
        replay(playback);

        FileLoader loader = FileLoader.forFormat(FileLoader.Format.TAP);
        java.nio.file.Path path = tapFile.toPath();
        loader.load(path, callbacks(playback), FileLoader.Options.playback());

        verify(playback);
    }

    @Test
    public void testLoadMultipleDataBlocks() throws IOException {
        byte[] data1 = new byte[]{0x01, 0x02};
        byte[] block1 = createTapBlock(0xFF, data1);
        byte[] data2 = new byte[]{0x03, 0x04, 0x05};
        byte[] block2 = createTapBlock(0xFF, data2);

        File tapFile = writeTapFile(block1, block2);

        TapeListener playback = createStrictMock(TapeListener.class);
        playback.onFileStart();
        playback.onDataStart();
        playback.onBlockFlag(0xFF);
        playback.onBlockData(aryEq(data1));
        playback.onBlockChecksum(anyByte());
        playback.onDataStart();
        playback.onBlockFlag(0xFF);
        playback.onBlockData(aryEq(data2));
        playback.onBlockChecksum(anyByte());
        playback.onFileEnd();
        replay(playback);

        FileLoader loader = FileLoader.forFormat(FileLoader.Format.TAP);
        java.nio.file.Path path = tapFile.toPath();
        loader.load(path, callbacks(playback), FileLoader.Options.playback());

        verify(playback);
    }

    @Test(expected = NullPointerException.class)
    public void testNullPathThrows() throws IOException {
        FileLoader.forFormat(FileLoader.Format.TAP).load(null, block -> { }, FileLoader.Options.playback());
    }

    @Test(expected = IOException.class)
    public void testLoadNonExistentFileThrows() throws IOException {
        FileLoader loader = FileLoader.forFormat(FileLoader.Format.TAP);
        java.nio.file.Path path = tempFolder.getRoot().toPath().resolve("nonexistent.tap");
        TapeListener playback = niceMock(TapeListener.class);
        replay(playback);
        loader.load(path, callbacks(playback), FileLoader.Options.playback());
    }
    private FileLoader.Listener callbacks(TapeListener listener) {
        return new FileLoader.Listener() {
            @Override public void onFileStart() { listener.onFileStart(); }
            @Override public void onBlock(FileLoader.Block block) throws IOException { TapeReader.emit(block, listener); }
            @Override public void onFileEnd() { listener.onFileEnd(); }
        };
    }

}

