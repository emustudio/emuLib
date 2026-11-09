/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib;

import org.junit.Test;

import static net.emustudio.emulib.RadixUtils.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class RadixUtilsTest {

    @Test
    public void testParseRadix() {
        assertEquals(32, parseRadix("0x20"));
        assertEquals(0xAB, parseRadix("0XaB"));
        assertEquals(32, parseRadix("20h"));
        assertEquals(0xB, parseRadix("bH"));
        assertEquals(0x0B1, parseRadix("0b1h"));
        assertEquals(5, parseRadix("101b"));
        assertEquals(5, parseRadix("0b101"));
        assertEquals(0, parseRadix("0b"));
        assertEquals(15, parseRadix("17o"));
        assertEquals(15, parseRadix("17Q"));
        assertEquals(12, parseRadix("12d"));
        assertEquals(12, parseRadix("012"));
        assertEquals(0, parseRadix("0"));
    }

    @Test
    public void testParseRadixSign() {
        assertEquals(-32, parseRadix("-0x20"));
        assertEquals(32, parseRadix("+20h"));
        assertEquals(-5, parseRadix("-101b"));
        assertEquals(-12, parseRadix("-12"));
        assertEquals(Integer.MIN_VALUE, parseRadix("-0x80000000"));
        assertEquals(Integer.MAX_VALUE, parseRadix("2147483647"));
    }

    @Test
    public void testParseRadixInvalid() {
        String[] invalid = {"", "-", "+", "ppp", "89o", "12b", "0x", "h", "d", "0x10h", "0x-1", "--1", " 1", "1 ",
                "1F", "2147483648", "0x80000000", "-0x80000001", "0xFFFFFFFFFF", "١٢"};
        for (String number : invalid) {
            try {
                parseRadix(number);
                fail("Expected NumberFormatException for '" + number + "'");
            } catch (NumberFormatException ignored) {
            }
        }
    }

    @Test
    public void testFormatHex() {
        assertEquals("01", formatHex(1, 2));
        assertEquals("1F", formatHex(0x1F, 2));
        assertEquals("0000", formatHex(0, 4));
        assertEquals("031F", formatHex(0x031F, 4));
        assertEquals("0E5A231F", formatHex(0x0E5A231F, 8));
        assertEquals("FE5A231F", formatHex(0xFE5A231F, 8));
        assertEquals("0", formatHex(0, 0));
        // never truncated
        assertEquals("123", formatHex(0x123, 2));
        assertEquals("FFFFFFFF", formatHex(-1, 2));
    }

    @Test
    public void testFormatBinary() {
        assertEquals("00100000", formatBinary(32, 8));
        assertEquals("00000000", formatBinary(0, 8));
        assertEquals("0000101111", formatBinary(0x2F, 10));
        assertEquals("101111", formatBinary(0x2F, 0));
        assertEquals("11111111111111111111111111111111", formatBinary(-1, 8));
    }

    @Test
    public void testFormatBinaryGrouped() {
        assertEquals("0010 0000", formatBinary(32, 8, 4));
        assertEquals("00 0010 1111", formatBinary(0x2F, 10, 4));
        assertEquals("0 000 101 111", formatBinary(0x2F, 10, 3));
        assertEquals("0000101111", formatBinary(0x2F, 10, 0));
        assertEquals("1", formatBinary(1, 1, 4));
    }
}
