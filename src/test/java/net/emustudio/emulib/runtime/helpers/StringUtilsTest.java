/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.helpers;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class StringUtilsTest {

    @Test
    public void testTextIsShortened() {
        String result = StringUtils.shorten("This is a long text", 10);
        assertEquals("Thi...text", result);
    }

    @Test
    public void testTextIsPreserved() {
        String result = StringUtils.shorten("This is a long text", 30);
        assertEquals("This is a long text", result);
    }

    @Test
    public void testFormatCharacter() {
        Bits bits = new Bits(65, 8);
        String result = StringUtils.format('c', bits);
        assertEquals(1, result.length());
    }

    @Test
    public void testFormatCharacterEmptyString() {
        Bits bits = new Bits(0, 8);
        String result = StringUtils.format('c', bits);
        assertTrue(result.length() <= 1);
    }

    @Test
    public void testFormatDecimal() {
        Bits bits = new Bits(42, 32);
        assertEquals("42", StringUtils.format('d', bits));
    }

    @Test
    public void testFormatDecimalNegative() {
        Bits bits = new Bits(-42, 32);
        assertEquals("-42", StringUtils.format('d', bits));
    }

    @Test
    public void testFormatFloat32() {
        Bits bits = new Bits(Float.floatToIntBits(3.14f), 32);
        String result = StringUtils.format('f', bits);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    public void testFormatFloat64() {
        double value = 3.14159265358979;
        long longBits = Double.doubleToLongBits(value);
        Bits bits = new Bits((int) longBits, 64);
        String result = StringUtils.format('f', bits);
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertNotEquals("NaN", result);
    }

    @Test
    public void testFormatFloatInvalidBitLength() {
        Bits bits = new Bits(42, 16);
        assertEquals("NaN", StringUtils.format('f', bits));
    }

    @Test
    public void testFormatString() {
        int value = ('H' << 24) | ('i' << 16);
        Bits bits = new Bits(value, 32);
        assertEquals("Hi", StringUtils.format('s', bits));
    }

    @Test
    public void testFormatStringWithNullTerminator() {
        Bits bits = new Bits(0x48690000, 32);
        String result = StringUtils.format('s', bits);
        assertFalse(result.contains("\0"));
    }

    @Test
    public void testFormatHexLowercase() {
        Bits bits = new Bits(255, 32);
        assertEquals("ff", StringUtils.format('x', bits));
    }

    @Test
    public void testFormatHexUppercase() {
        Bits bits = new Bits(255, 32);
        assertEquals("FF", StringUtils.format('X', bits));
    }

    @Test
    public void testFormatPercent() {
        Bits bits = new Bits(42, 8);
        assertEquals("%", StringUtils.format('%', bits));
    }

    @Test
    public void testFormatUnknownCharacter() {
        Bits bits = new Bits(42, 8);
        assertEquals("z", StringUtils.format('z', bits));
    }

    @Test
    public void testFormatZeroValue() {
        Bits bits = new Bits(0, 32);
        assertEquals("0", StringUtils.format('d', bits));
        assertEquals("0", StringUtils.format('x', bits));
        assertEquals("0", StringUtils.format('X', bits));
    }

    @Test
    public void testFormatLargeHexValue() {
        Bits bits = new Bits(0xABCDEF, 32);
        assertEquals("abcdef", StringUtils.format('x', bits));
        assertEquals("ABCDEF", StringUtils.format('X', bits));
    }

}
