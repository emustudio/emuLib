/*
 * KISS, YAGNI, DRY
 *
 * (c) Copyright 2006-2016, Peter Jakubčo
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

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class RadixUtilsTest {

    @Before
    public void setUp() throws Exception {
        RadixUtils.getInstance().setDefaults();
    }

    @Test
    public void testConvertToRadix10to16() throws Exception {
        assertEquals("CA0", RadixUtils.convertToRadix(new byte[]{ (byte)0xA0, 0xC }, 16, true));
        assertEquals("F0F", RadixUtils.convertToRadix(new byte[] { 0xF, 0xF }, 16, true));

        // big endian
        assertEquals("201", RadixUtils.convertToRadix(new byte[]{2, 1}, 16, false));
    }

    @Test
    public void testConvertToRadix10to8() throws Exception {
        assertEquals("40", RadixUtils.convertToRadix(new byte[] { 040 }, 8, true));
    }

    @Test
    public void testConvertToRadix10to3() throws Exception {
        assertEquals("1012", RadixUtils.convertToRadix(new byte[] { 32 }, 3, true));
    }

    @Test
    public void testConvertToRadix10to10() {
        assertEquals("300", RadixUtils.convertToRadix(new byte[]{0x2C, 1}, 10, true));
    }

    @Test
    public void testConvertToRadixString10to16() {
        assertEquals("22DF0", RadixUtils.getInstance().convertToRadix("142832", 16));
    }

    @Test
    public void testConvertToRadixString2to10() {
        assertEquals("5", RadixUtils.convertToRadix("101", 2, 10));
    }

    @Test
    public void testConvertToNumberFrom10to10() throws Exception {
        byte[] expected = new byte[] { 0x2C, 1 };
        byte[] result = RadixUtils.convertToNumber("300", 10);

        assertArrayEquals(expected, result);
    }

    @Test
    public void testConvertToNumberFrom16to10() throws Exception {
        byte[] expected = new byte[] { (byte)0xF0, 0x2D, 2 };
        byte[] result = RadixUtils.convertToNumber("22DF0", 16);

        assertArrayEquals(expected, result);
    }

    @Test
    public void testConvertToNumberFrom2to10() throws Exception {
        byte[] expected = new byte[] { (byte)214 };
        byte[] result = RadixUtils.convertToNumber("11010110", 2);

        assertArrayEquals(expected, result);
    }

    @Test
    public void testConvertToNumberZero() throws Exception {
        byte[] expected = new byte[] { 0 };
        byte[] result = RadixUtils.convertToNumber("00000000000000000000000000000000", 2);

        assertArrayEquals(expected, result);
    }

    @Test
    public void testConvertToNumberZeroWithBytesCount() throws Exception {
        byte[] expected = new byte[] { 0,0,0,0 };
        byte[] result = RadixUtils.convertToNumber("000", 2, 4);

        assertArrayEquals(expected, result);
    }

    @Test
    public void testConvertToNumberWithBytesCount() throws Exception {
        byte[] expected = new byte[] { 9,0,0,0 };
        byte[] result = RadixUtils.convertToNumber("1001", 2, 4);

        assertArrayEquals(expected, result);
    }

    @Test
    public void testParseRadix() {
        String number = "0x20";
        int expResult = 32;
        RadixUtils instance = RadixUtils.getInstance();
        int result = instance.parseRadix(number);
        assertEquals(expResult, result);
        
        number = "20h";
        expResult = 32;
        result = instance.parseRadix(number);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testGetDwordHexString() {
        int number = 1;
        assertEquals("00000001", RadixUtils.getDwordHexString(number));
        
        number = 0x0E5A231F;
        assertEquals("0E5A231F", RadixUtils.getDwordHexString(number));

        number = 0xFE5A231F;
        assertEquals("FE5A231F", RadixUtils.getDwordHexString(number));
    }

    @Test
    public void testGetWordHexString() {
        int number = 1;
        assertEquals("0001", RadixUtils.getWordHexString(number));
        
        number = 0x031F;
        assertEquals("031F", RadixUtils.getWordHexString(number));

        number = 0x231F;
        assertEquals("231F", RadixUtils.getWordHexString(number));
    }

    @Test
    public void testGetWordHexStringUpperLower() throws Exception {
        short upper = 1;
        short lower = 2;

        assertEquals("0102", RadixUtils.getWordHexString(upper, lower));
    }

    @Test
    public void testGetByteHexString() {
        int number = 1;
        assertEquals("01", RadixUtils.getByteHexString(number));
        
        number = 0x1F;
        assertEquals("1F", RadixUtils.getByteHexString(number));
    }

    @Test(expected = NumberFormatException.class)
    public void testParseUnknownRadixThrows() throws Exception {
        RadixUtils.getInstance().parseRadix("ppp");
    }

    @Test
    public void testConvertToRadixSameRadixesReturnUnmodifiedInputImmediately() throws Exception {
        assertEquals("bullshit", RadixUtils.convertToRadix("bullshit", 18, 18));
    }

    @Test(expected = NumberFormatException.class)
    public void testConvertToRadixUnrecognizedNumber() throws Exception {
        RadixUtils.getInstance().convertToRadix("ppp", 99);
    }

    @Test
    public void testConvertToRadixSameRadixAsFoundPattern() throws Exception {
        assertEquals("15", RadixUtils.getInstance().convertToRadix("15h", 16));
    }

    @Test
    public void testAddBinaryPattern() throws Exception {
        RadixUtils radixUtils = RadixUtils.getInstance();
        radixUtils.addNumberPattern(new RadixUtils.NumberPattern("[01]+b", 2, 0, 1));
        radixUtils.addNumberPattern(new RadixUtils.NumberPattern("[01]+", 2, 0, 0));

        assertEquals(6, radixUtils.parseRadix("110b", 2));
        assertEquals(6, radixUtils.parseRadix("110", 2));
    }

    @Test(expected = NumberFormatException.class)
    public void testParseNonexistantRadixThrows() throws Exception {
        RadixUtils.getInstance().parseRadix("15h", 18);
    }
}

