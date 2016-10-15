/*
 * KISS, YAGNI, DRY
 *
 * (c) Copyright 2006-2016, Peter Jakubčo
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package emulib.runtime;

import emulib.runtime.NumberUtils.Strategy;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class NumberUtilsTest {
    
    @Test
    public void testReverseBits() {
        assertEquals(3, NumberUtils.reverseBits(6, 3));
        assertEquals(0xD96D, NumberUtils.reverseBits(0xB69B, 16));
        assertEquals(0xD9, NumberUtils.reverseBits(0xB69B, 8));
        assertEquals(0xD96DD96D, NumberUtils.reverseBits(0xB69BB69B, 32));
    }
    
    @Test
    public void testReadInt() {
        Integer[] word = new Integer[] { 0xAB, 0xCD, 0xEF, 0x12 };
        assertEquals(0x12EFCDAB, NumberUtils.readInt(word, Strategy.LITTLE_ENDIAN));
        
        Byte[] word2 = new Byte[] { 0xB, 6, 9, 0xB };
        assertEquals(0x0B06090B, NumberUtils.readInt(word2, Strategy.BIG_ENDIAN));
        
        assertEquals(0xD06090D0, NumberUtils.readInt(word2, Strategy.REVERSE_BITS));
        assertEquals(0xD09060D0, NumberUtils.readInt(word2, Strategy.REVERSE_BITS | Strategy.BIG_ENDIAN));
    }
    
    @Test(expected = IndexOutOfBoundsException.class)
    public void testReadIntFewBytes() {
        Integer[] word = new Integer[] { 0xAB, 0xCD };
        NumberUtils.readInt(word, Strategy.LITTLE_ENDIAN);
    }
    
    @Test
    public void testReadIntDoesNotTakeMoreBytesIntoAccount() {
        Integer[] word = new Integer[] { 0xAB, 0xCD, 0xEF, 0x12, 0x30, 0x50, 0xAA };
        assertEquals(0x12EFCDAB, NumberUtils.readInt(word, Strategy.LITTLE_ENDIAN));
    }
        
    @Test
    public void testWriteInt() {
        Integer[] word = new Integer[4];
        NumberUtils.writeInt(0x12EFCDAB, word, Strategy.LITTLE_ENDIAN);
        assertArrayEquals(new Integer[] { 0xAB, 0xCD, 0xEF, 0x12 }, word);
        
        Byte[] word2 = new Byte[4];
        NumberUtils.writeInt(0x0B06090B, word2, Strategy.BIG_ENDIAN);
        assertArrayEquals(new Byte[] { 0xB, 6, 9, 0xB }, word2);
        
        Short[] word3 = new Short[4];
        NumberUtils.writeInt(0x0B06090B, word3, Strategy.BIG_ENDIAN);
        assertArrayEquals(new Short[] { 0xB, 6, 9, 0xB }, word3);

        NumberUtils.writeInt(0xD06090D0, word2, Strategy.REVERSE_BITS);
        assertArrayEquals(new Byte[] { 0xB, 9, 6, 0xB }, word2);
        
        NumberUtils.writeInt(0xD09060D0, word2, Strategy.REVERSE_BITS | Strategy.BIG_ENDIAN);
        assertArrayEquals(new Byte[] { 0xB, 9, 6, 0xB }, word2);
    }
    
    @Test(expected = IndexOutOfBoundsException.class)
    public void testWriteIntFewBytes() {
        Byte[] word = new Byte[2];
        NumberUtils.writeInt(2, word, Strategy.LITTLE_ENDIAN);
    }
    
    @Test
    public void testWriteIntDoesNotModifyOtherBytes() {
        Byte[] word = new Byte[] {1,2,3,4,5,6,7,8,9};
        NumberUtils.writeInt(0x0B06090B, word, Strategy.BIG_ENDIAN);
        assertArrayEquals(new Byte[] { 0xB, 6, 9, 0xB, 5,6,7,8,9 }, word);
    }
    
}
