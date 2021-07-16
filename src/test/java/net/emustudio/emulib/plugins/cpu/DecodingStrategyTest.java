package net.emustudio.emulib.plugins.cpu;

import org.junit.Test;

import static net.emustudio.emulib.plugins.cpu.DecodingStrategy.*;
import static org.junit.Assert.assertEquals;

public class DecodingStrategyTest {

    @Test
    public void testAbsoluteStrategyWorks() {
        assertEquals(0x1D, absolute(0x1D));
        assertEquals(127, absolute((byte)0x81));
    }

    @Test
    public void testBitReverseWorks() {
        assertEquals(0b11100000, reverseBits(0b000111));
    }

    @Test
    public void testShiftLeftWorks() {
        assertEquals(0x0402, shiftLeft(0x0201));
    }

    @Test
    public void testShiftRightWorks() {
        assertEquals(0x0100, shiftRight(0x0201));
    }

    @Test
    public void testReverseBytesWorks() {
        assertEquals(0x01020304, reverseBytes(0x04030201));
    }
}
