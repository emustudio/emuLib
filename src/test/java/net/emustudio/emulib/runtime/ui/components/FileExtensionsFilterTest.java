/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.ui.components;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class FileExtensionsFilterTest {

    @Test
    public void testConstructorWithVarargs() {
        FileExtensionsFilter filter = new FileExtensionsFilter("Image files", "jpg", "png", "gif");

        assertEquals("Image files", filter.getDescription());
        assertEquals(3, filter.getExtensions().size());
        assertTrue(filter.getExtensions().contains("jpg"));
        assertTrue(filter.getExtensions().contains("png"));
        assertTrue(filter.getExtensions().contains("gif"));
    }

    @Test
    public void testConstructorWithCollection() {
        List<String> extensions = Arrays.asList("txt", "md", "doc");
        FileExtensionsFilter filter = new FileExtensionsFilter("Text files", extensions);

        assertEquals("Text files", filter.getDescription());
        assertEquals(3, filter.getExtensions().size());
        assertTrue(filter.getExtensions().contains("txt"));
        assertTrue(filter.getExtensions().contains("md"));
        assertTrue(filter.getExtensions().contains("doc"));
    }

    @Test
    public void testGetDescription() {
        FileExtensionsFilter filter = new FileExtensionsFilter("Audio files", "mp3", "wav");
        assertEquals("Audio files", filter.getDescription());
    }

    @Test
    public void testGetExtensions() {
        FileExtensionsFilter filter = new FileExtensionsFilter("Video files", "mp4", "avi", "mkv");
        List<String> extensions = filter.getExtensions();

        assertNotNull(extensions);
        assertEquals(3, extensions.size());
        assertEquals("mp4", extensions.get(0));
        assertEquals("avi", extensions.get(1));
        assertEquals("mkv", extensions.get(2));
    }

    @Test
    public void testExtensionsAreImmutable() {
        FileExtensionsFilter filter = new FileExtensionsFilter("Test files", "test");
        List<String> extensions = filter.getExtensions();

        try {
            extensions.add("another");
            fail("Extensions list should be immutable");
        } catch (UnsupportedOperationException e) {
            // Expected
        }
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorWithNullDescription() {
        new FileExtensionsFilter(null, "ext");
    }

    @Test
    public void testConstructorWithEmptyExtensions() {
        FileExtensionsFilter filter = new FileExtensionsFilter("No extensions");

        assertEquals("No extensions", filter.getDescription());
        assertEquals(0, filter.getExtensions().size());
    }

    @Test
    public void testConstructorWithSingleExtension() {
        FileExtensionsFilter filter = new FileExtensionsFilter("Single extension", "xml");

        assertEquals("Single extension", filter.getDescription());
        assertEquals(1, filter.getExtensions().size());
        assertEquals("xml", filter.getExtensions().get(0));
    }

    @Test
    public void testMultipleFiltersWithSameExtensions() {
        FileExtensionsFilter filter1 = new FileExtensionsFilter("Filter 1", "txt");
        FileExtensionsFilter filter2 = new FileExtensionsFilter("Filter 2", "txt");

        assertEquals("Filter 1", filter1.getDescription());
        assertEquals("Filter 2", filter2.getDescription());
        assertEquals(filter1.getExtensions(), filter2.getExtensions());
    }

    @Test
    public void testExtensionsOrder() {
        FileExtensionsFilter filter = new FileExtensionsFilter("Ordered", "z", "a", "m");
        List<String> extensions = filter.getExtensions();

        // Order should be preserved
        assertEquals("z", extensions.get(0));
        assertEquals("a", extensions.get(1));
        assertEquals("m", extensions.get(2));
    }

    @Test
    public void testCollectionConstructorCreatesDefensiveCopy() {
        List<String> originalList = Arrays.asList("ext1", "ext2");
        FileExtensionsFilter filter = new FileExtensionsFilter("Test", originalList);

        // Verify it's a copy by checking the extensions list is independent
        List<String> extensions = filter.getExtensions();
        assertEquals(2, extensions.size());

        // Original list and filter's list should be independent
        assertNotSame(originalList, extensions);
    }
}
