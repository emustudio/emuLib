/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.ui;

import net.jcip.annotations.NotThreadSafe;

import java.util.*;

/**
 * Bounded history of distinct values, ordered from most to least recently inserted.
 *
 * @param <V> type of cached values
 */
@NotThreadSafe
public class MaxItemsCache<V> {
    private final LinkedList<V> cache = new LinkedList<>();

    private final int limit;

    /**
     * Constructs a new cache.
     *
     * @param limit the cache is allowed to contain only given number of items
     */
    public MaxItemsCache(int limit) {
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
     * Moves an existing value to the front and removes the oldest value if the limit is exceeded.
     *
     * @param value value to be cached
     */
    public void put(V value) {
        cache.remove(value);
        cache.addFirst(value);
        if (cache.size() > limit) {
            cache.removeLast();
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
        return cache.iterator();
    }

    /**
     * Get the first (the most recently used) value from the cache, if it exists.
     *
     * @return the most recently used value from the cache
     */
    public Optional<V> first() {
        return cache.isEmpty() ? Optional.empty() : Optional.of(cache.getFirst());
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


