/*
 * UniversalFileFilterTest.java
 *
 * KISS, YAGNI, DRY
 *
 * (c) Copyright 2013, Peter Jakubčo
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

import java.io.File;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

public class UniversalFileFilterTest {
    private UniversalFileFilter filter;
    
    @Before
    public void setUp() {
        filter = new UniversalFileFilter();
    }
    
    @Test
    public void testFirstExtension() {
        String extension = "bmp";
        filter.addExtension(extension);
        filter.addExtension("jpg");
        assertEquals(extension, filter.getFirstExtension());
    }

    @Test
    public void testExtensionCountForEqualExtensions() {
        String extension = "bmp";
        filter.addExtension(extension);
        filter.addExtension(extension);
        assertEquals(1, filter.getExtensionsCount());
    }
    
    @Test
    public void testGetExtensionForFile() {
        File f = new File("asdsad.xxx");
        assertEquals("xxx", UniversalFileFilter.getExtension(f));
    }
    
    @Test
    public void testExtensionWithDots() {
        String extension = "bmp";
        String dottedExtension = "...bmp";
        filter.addExtension(extension);
        filter.addExtension(dottedExtension);
        assertEquals(extension, filter.getFirstExtension());
        assertEquals(1, filter.getExtensionsCount());
    }
    
    @Test
    public void testAccept() {
        String extension = "bmp";
        filter.addExtension(extension);
        File f = new File("sample." + extension);
        
        assertTrue(filter.accept(f));
    }
    
    @Test
    public void testNotAccept() {
        String extension = "bmp";
        filter.addExtension(extension);
        File f = new File("sample.afsdljf");
        
        assertFalse(filter.accept(f));
    }
    
    @Test
    public void testAcceptAll() {
        String extension = "*";
        filter.addExtension(extension);
        File f = new File("sample.bmp");
        
        assertTrue(filter.accept(f));
    }
    
    @Test
    public void testAcceptAllFileWithoutExtension() {
        String extension = "*";
        filter.addExtension("bmp");
        filter.addExtension(extension);
        File f = new File("sample");
        
        assertTrue(filter.accept(f));
    }
    
    @Test
    public void testAcceptNoExtensionsAllowed() {
        File f = new File("sample");
        assertFalse(filter.accept(f));
    }
    
    @Test
    public void testFirstExceptionReturnsNullWhenNoExtensions() {
        assertNull(filter.getFirstExtension());
    }
    
    @Test
    public void testSetAndGetDescription() {
        filter.setDescription("AAA");
        assertEquals("AAA", filter.getDescription());
    }
    
}
