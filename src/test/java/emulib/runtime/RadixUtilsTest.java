/*
 * RadixUtilsTest.java
 *
 * KISS, YAGNI, DRY
 *
 * (c) Copyright 2012-2013, Peter Jakubčo
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

import static org.junit.Assert.*;
import org.junit.Test;

public class RadixUtilsTest {
    
    /**
     * Test of convertToRadix method, of class RadixUtils.
     */
    @Test
    public void testConvertToRadix_shortArr_int() {
        byte[] radix10Number = { (byte)0xA0, 0xC };
        int toRadix = 16;
        String expResult = "CA0";
        String result = RadixUtils.convertToRadix(radix10Number, toRadix, true);
        assertEquals(expResult, result);
        
        radix10Number = new byte[] { 0xF, 0xF };
        expResult = "F0F";
        result = RadixUtils.convertToRadix(radix10Number, toRadix, true);
        assertEquals(expResult, result);
        
        radix10Number = new byte[] { 040 };
        expResult = "40";
        toRadix = 8;
        result = RadixUtils.convertToRadix(radix10Number, toRadix, true);
        assertEquals(expResult, result);

        radix10Number = new byte[] { 2, 1 }; // 201h stored as 2-byte int
        expResult = "201";
        toRadix = 16;
        result = RadixUtils.convertToRadix(radix10Number, toRadix, false);
        assertEquals(expResult, result);
        
        radix10Number = new byte[] { 32 };
        expResult = "1012";
        toRadix = 3;
        result = RadixUtils.convertToRadix(radix10Number, toRadix, true);
        assertEquals(expResult, result);
        
        radix10Number = new byte[] { 0x2C, 1 };
        expResult = "300";
        toRadix = 10;
        result = RadixUtils.convertToRadix(radix10Number, toRadix, true);
        assertEquals(expResult, result);
    }

    /**
     * Test of convertToRadix method, of class RadixUtils.
     */
    @Test
    public void testConvertToRadix_String_int() {
        String number = "142832";
        int toRadix = 16;
        String expResult = "22DF0";
        RadixUtils instance = RadixUtils.getInstance();
        String result = instance.convertToRadix(number, toRadix);
        assertEquals(expResult, result);
    }

    /**
     * Test of convertToRadix method, of class RadixUtils.
     */
    @Test
    public void testConvertToRadix_3args() {
        String number = "101";
        int fromRadix = 2;
        int toRadix = 10;
        String expResult = "5";
        String result = RadixUtils.convertToRadix(number, fromRadix, toRadix);
        assertEquals(expResult, result);
    }

    @Test
    public void testConvertToNumber() {
        String number = "300";
        int fromRadix = 10;
        byte[] expResult = new byte[] { 0x2C, 1 };
        byte[] result = RadixUtils.convertToNumber(number, fromRadix);
        
        assertTrue(expResult.length == result.length);
        
        for (int i = 0; i < expResult.length; i++) {
            assertEquals(expResult[i], result[i]);
        }
        
        number = "142832";
        fromRadix = 10;
        expResult = new byte[] { (byte)240, 45, 2 };
        result = RadixUtils.convertToNumber(number, fromRadix);
        
        assertTrue(expResult.length == result.length);
        
        for (int i = 0; i < expResult.length; i++) {
            assertEquals(expResult[i], result[i]);
        }

        number = "11010110";
        fromRadix = 2;
        expResult = new byte[] { (byte)214 };
        result = RadixUtils.convertToNumber(number, fromRadix);
        
        assertTrue(expResult.length == result.length);
        
        for (int i = 0; i < expResult.length; i++) {
            assertEquals(expResult[i], result[i]);
        }
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
    
    
}
