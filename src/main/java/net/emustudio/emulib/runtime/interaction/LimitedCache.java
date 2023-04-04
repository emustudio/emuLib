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

import net.jcip.annotations.NotThreadSafe;

import java.util.*;

/**
 * Limited Cache
 * A cache with max items limit, retaining most used items, and removing the least used items above given limit.
 */
@NotThreadSafe
public class LimitedCache<V> {
    private final SortedMap<Integer, V> cache = new TreeMap<>();

    private final int limit;

    /**
     * Constructs a new cache.
     *
     * @param limit the cache is allowed to contain only given number of items
     */
    public LimitedCache(int limit) {
        this.limit = limit;
    }

    /**
     * Clears the cache
     */
    public void clear() {
        cache.clear();
    }

    /**
     * Adds new value to the cache.
     * If the cache items are above the limit, the least used item is removed.
     * Added value is always considered as the most used.
     *
     * @param value value to be cached
     */
    public void put(V value) {
        Map<V, Integer> newCache = new HashMap<>();

        int index = 0;
        newCache.put(value, index++);
        for (Map.Entry<Integer, V> e : cache.entrySet()) {
            if (!e.getValue().equals(value)) {
                newCache.put(e.getValue(), index++);
            }
        }

        cache.clear();
        for (Map.Entry<V, Integer> e : newCache.entrySet()) {
            cache.put(e.getValue(), e.getKey());
        }

        if (index > limit) {
            cache.remove(cache.lastKey());
        }
    }

    /**
     * Creates an iterator of values stored in this cache.
     * The values are returned in the "most recently used" order.
     * A value inserted more than once will not increase cache size.
     *
     * @return cached values iterator
     */
    public Iterator<V> iterator() {
        return cache.values().iterator();
    }

    /**
     * Get the first (the most recently used) value from the cache, if it exists.
     *
     * @return the most recently used value from the cache
     */
    public Optional<V> first() {
        Iterator<V> it = iterator();
        if (it.hasNext()) {
            return Optional.of(it.next());
        }
        return Optional.empty();
    }

    /**
     * Gets the cache limit
     *
     * @return cache limit
     */
    public int getLimit() {
        return limit;
    }

    /**
     * Gets the cache size (number of items stored in the cache).
     * If a value was stored multiple times, it is considered as put once.
     *
     * @return cache size
     */
    public int getSize() {
        return cache.size();
    }
}


