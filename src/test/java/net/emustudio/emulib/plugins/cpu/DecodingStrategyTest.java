package net.emustudio.emulib.plugins.cpu;

import org.junit.Test;

import static net.emustudio.emulib.plugins.cpu.DecodingStrategy.*;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class DecodingStrategyTest {

    @Test
    public void testAbsoluteStrategyWorks() {
        assertEquals(0x1D, absolute(new byte[] { 0x1D })[0]);
        assertEquals(127, absolute(new byte[] { (byte)0x81 })[0]);
    }

    @Test
    public void testBitReverseWorks() {
        assertEquals((byte)0b11100000, reverseBits(new byte[] { 0b000111})[0]);
    }

    @Test
    public void testShiftLeftWorks() {
        assertArrayEquals(new byte[] { 2, 4 }, shiftLeft(new byte[] { 1,2 }));
    }

    @Test
    public void testShiftRightWorks() {
        assertArrayEquals(new byte[] { 0, (byte)0b10000001 }, shiftRight(new byte[] { 1,2 }));
    }

    @Test
    public void testReverseBytesWorks() {
        assertArrayEquals(new byte[] { 0x4, 0x3, 0x2, 0x1}, reverseBytes(new byte[] { 0x1, 0x2, 0x3, 0x4}));
    }
}
