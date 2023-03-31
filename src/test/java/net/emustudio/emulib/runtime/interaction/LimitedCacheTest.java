/*
 * This file is part of emuLib.
 *
 * Copyright (C) 2006-2023  Peter Jakubčo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.emustudio.emulib.runtime.interaction;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

public class LimitedCacheTest {

    @Test
    public void testClearCache() {
        LimitedCache<Integer> cache = new LimitedCache<>(2);
        cache.put(0);
        assertEquals(1, cache.getSize());

        cache.clear();
        assertEquals(0, cache.getSize());
    }

    @Test
    public void testEmptyCachePut() {
        LimitedCache<Integer> cache = new LimitedCache<>(2);
        cache.put(10);

        Iterator<Integer> it = cache.iterator();
        assertTrue(it.hasNext());

        assertEquals(10, it.next().intValue());
        assertFalse(it.hasNext());
    }

    @Test
    public void testPutTheSameValueTwice() {
        LimitedCache<Integer> cache = new LimitedCache<>(2);
        cache.put(10);
        cache.put(10);

        assertEquals(1, cache.getSize());
    }

    @Test
    public void testCorrectOrderingOnRetrieval() {
        LimitedCache<Integer> cache = new LimitedCache<>(5);
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
        LimitedCache<Integer> cache = new LimitedCache<>(2);
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
        LimitedCache<Integer> cache = new LimitedCache<>(0);

        cache.put(10);
        assertEquals(0, cache.getSize());
        assertFalse(cache.iterator().hasNext());
    }
}
