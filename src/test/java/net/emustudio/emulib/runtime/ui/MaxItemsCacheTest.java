/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.ui;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

public class MaxItemsCacheTest {

    @Test
    public void testClearCache() {
        MaxItemsCache<Integer> cache = new MaxItemsCache<>(2);
        cache.put(0);
        assertEquals(1, cache.getSize());

        cache.clear();
        assertEquals(0, cache.getSize());
    }

    @Test
    public void testEmptyCachePut() {
        MaxItemsCache<Integer> cache = new MaxItemsCache<>(2);
        cache.put(10);

        Iterator<Integer> it = cache.iterator();
        assertTrue(it.hasNext());

        assertEquals(10, it.next().intValue());
        assertFalse(it.hasNext());
    }

    @Test
    public void testPutTheSameValueTwice() {
        MaxItemsCache<Integer> cache = new MaxItemsCache<>(2);
        cache.put(10);
        cache.put(10);

        assertEquals(1, cache.getSize());
    }

    @Test
    public void testCorrectOrderingOnRetrieval() {
        MaxItemsCache<Integer> cache = new MaxItemsCache<>(5);
        cache.put(10);
        cache.put(10);
        cache.put(1);
        cache.put(3);

        List<Integer> returned = new ArrayList<>();
        Iterator<Integer> it = cache.iterator();
        while (it.hasNext()) {
            returned.add(it.next());
        }
        assertArrayEquals(new Integer[]{
                3, 1, 10
        }, returned.toArray(new Integer[0]));
    }

    @Test
    public void testLimit() {
        MaxItemsCache<Integer> cache = new MaxItemsCache<>(2);
        assertEquals(2, cache.getLimit());

        cache.put(1);
        cache.put(2);
        cache.put(3);

        assertEquals(2, cache.getSize());

        List<Integer> returned = new ArrayList<>();
        Iterator<Integer> it = cache.iterator();
        while (it.hasNext()) {
            returned.add(it.next());
        }
        assertArrayEquals(new Integer[]{
                3, 2
        }, returned.toArray(new Integer[0]));
    }

    @Test
    public void testZeroItemsLimit() {
        MaxItemsCache<Integer> cache = new MaxItemsCache<>(0);

        cache.put(10);
        assertEquals(0, cache.getSize());
        assertFalse(cache.iterator().hasNext());
    }
}
