/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package emulib.runtime;

import junit.framework.TestCase;

/**
 *
 * @author vbmacher
 */
public class RadixUtilsTest extends TestCase {
    
    public RadixUtilsTest(String testName) {
        super(testName);
    }

    /**
     * Test of convertToRadix method, of class RadixUtils.
     */
    public void testConvertToRadix_shortArr_int() {
        short[] radix10Number = { 0xA0, 0xC };
        int toRadix = 16;
        String expResult = "CA0";
        String result = RadixUtils.convertToRadix(radix10Number, toRadix);
        assertEquals(expResult, result);
        
        radix10Number = new short[] { 0xF, 0xF };
        expResult = "F0F";
        result = RadixUtils.convertToRadix(radix10Number, toRadix);
        assertEquals(expResult, result);
        
        radix10Number = new short[] { 040 };
        expResult = "40";
        toRadix = 8;
        result = RadixUtils.convertToRadix(radix10Number, toRadix);
        assertEquals(expResult, result);

        radix10Number = new short[] { 2, 1 }; // 258 stored as 2-byte int
        expResult = "402";
        toRadix = 8;
        result = RadixUtils.convertToRadix(radix10Number, toRadix);
        assertEquals(expResult, result);
        
        radix10Number = new short[] { 32 };
        expResult = "1012";
        toRadix = 3;
        result = RadixUtils.convertToRadix(radix10Number, toRadix);
        assertEquals(expResult, result);
        
        radix10Number = new short[] { 0x2C, 1 };
        expResult = "300";
        toRadix = 10;
        result = RadixUtils.convertToRadix(radix10Number, toRadix);
        assertEquals(expResult, result);
    }

    /**
     * Test of convertToRadix method, of class RadixUtils.
     */
    public void testConvertToRadix_String_int() {
        String number = "142832";
        int toRadix = 16;
        String expResult = "22DF0";
        RadixUtils instance = new RadixUtils();
        String result = instance.convertToRadix(number, toRadix);
        assertEquals(expResult, result);
    }

    /**
     * Test of convertToRadix method, of class RadixUtils.
     */
    public void testConvertToRadix_3args() {
        String number = "101";
        int fromRadix = 2;
        int toRadix = 10;
        String expResult = "5";
        String result = RadixUtils.convertToRadix(number, fromRadix, toRadix);
        assertEquals(expResult, result);
    }

    /**
     * Test of convertToNumber method, of class RadixUtils.
     */
    public void testConvertToNumber() {
        String number = "300";
        int fromRadix = 10;
        short[] expResult = new short[] { 0x2C, 1 };
        short[] result = RadixUtils.convertToNumber(number, fromRadix);
        
        assertTrue(expResult.length == result.length);
        
        for (int i = 0; i < expResult.length; i++) {
            assertEquals(expResult[i], result[i]);
        }
        
        number = "142832";
        fromRadix = 10;
        expResult = new short[] { 240, 45, 2 };
        result = RadixUtils.convertToNumber(number, fromRadix);
        
        assertTrue(expResult.length == result.length);
        
        for (int i = 0; i < expResult.length; i++) {
            assertEquals(expResult[i], result[i]);
        }
    }

    /**
     * Test of parseRadix method, of class RadixUtils.
     */
    public void testParseRadix() {
        String number = "0x20";
        int expResult = 32;
        RadixUtils instance = new RadixUtils();
        int result = instance.parseRadix(number);
        assertEquals(expResult, result);
    }
}
