/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.annotations;

import net.emustudio.emulib.plugins.compiler.SourceCodePosition;
import net.jcip.annotations.ThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/** Thread-safe live annotations at CPU-visible addresses. */
@ThreadSafe
public final class MemoryAnnotations {
    private static final Logger LOGGER = LoggerFactory.getLogger(MemoryAnnotations.class);
    private static final int PAGE_SHIFT = 8;
    private static final int MAX_INDEXED_PAGES = 1024;
    /** An annotation covers the half-open address range [start, end). */
    public static final class Entry {
        public final long ownerId;
        public final long entryId;
        public final int start;
        public final int end;
        public final String kind;
        public final String text;
        public final SourceCodePosition source;

        public Entry(long ownerId, long entryId, int start, int end, String kind, String text,
                     SourceCodePosition source) {
            if (start < 0 || end <= start) throw new IllegalArgumentException("Invalid address range");
            this.ownerId = ownerId;
            this.entryId = entryId;
            this.start = start;
            this.end = end;
            this.kind = Objects.requireNonNull(kind);
            this.text = Objects.requireNonNull(text);
            this.source = source;
        }
    }

    private final Object lock = new Object();
    private final Map<Long, Map<Long, Entry>> entries = new LinkedHashMap<>();
    private final Map<Integer, Set<Entry>> addressPages = new HashMap<>();
    private final Set<Entry> wideEntries = new LinkedHashSet<>();
    private final List<Runnable> listeners = new CopyOnWriteArrayList<>();
    private long revision;

    public long revision() {
        synchronized (lock) { return revision; }
    }

    public Entry get(long ownerId, long entryId) {
        synchronized (lock) {
            Map<Long, Entry> owned = entries.get(ownerId);
            return owned == null ? null : owned.get(entryId);
        }
    }

    /** Returns current entries covering the given address. */
    public List<Entry> at(int address) {
        synchronized (lock) {
            List<Entry> result = new ArrayList<>();
            for (Entry entry : addressPages.getOrDefault(address >> PAGE_SHIFT, Collections.emptySet()))
                if (entry.start <= address && address < entry.end) result.add(entry);
            for (Entry entry : wideEntries)
                if (entry.start <= address && address < entry.end) result.add(entry);
            return Collections.unmodifiableList(result);
        }
    }

    /** Returns all entries currently in the engine. */
    public List<Entry> all() {
        synchronized (lock) {
            List<Entry> result = new ArrayList<>();
            for (Map<Long, Entry> owned : entries.values()) result.addAll(owned.values());
            return Collections.unmodifiableList(result);
        }
    }

    public void put(Entry entry) {
        Objects.requireNonNull(entry);
        synchronized (lock) {
            Entry previous = entries.computeIfAbsent(entry.ownerId, ignored -> new LinkedHashMap<>())
                    .put(entry.entryId, entry);
            if (previous != null) unindex(previous);
            index(entry);
            revision++;
        }
        notifyListeners();
    }

    public void remove(long ownerId, long entryId) {
        synchronized (lock) {
            Map<Long, Entry> owned = entries.get(ownerId);
            if (owned == null) return;
            Entry removed = owned.remove(entryId);
            if (removed == null) return;
            unindex(removed);
            if (owned.isEmpty()) entries.remove(ownerId);
            revision++;
        }
        notifyListeners();
    }

    /** Replaces all entries for an owner in one atomic update. */
    public void replaceOwner(long ownerId, Collection<Entry> replacement) {
        Map<Long, Entry> next = validateReplacement(ownerId, replacement);
        synchronized (lock) {
            replaceOwnerLocked(ownerId, next);
        }
        notifyListeners();
    }

    /** Replaces an owner's entries only if the engine revision is still current. */
    public boolean compareAndReplaceOwner(long expected, long ownerId, Collection<Entry> replacement) {
        Map<Long, Entry> next = validateReplacement(ownerId, replacement);
        synchronized (lock) {
            if (revision != expected) return false;
            replaceOwnerLocked(ownerId, next);
        }
        notifyListeners();
        return true;
    }

    private static Map<Long, Entry> validateReplacement(long ownerId, Collection<Entry> replacement) {
        Objects.requireNonNull(replacement);
        Map<Long, Entry> next = new LinkedHashMap<>();
        for (Entry entry : replacement) {
            Objects.requireNonNull(entry);
            if (entry.ownerId != ownerId) throw new IllegalArgumentException("Entry has a different owner");
            if (next.put(entry.entryId, entry) != null) throw new IllegalArgumentException("Duplicate entry ID");
        }
        return next;
    }

    private void replaceOwnerLocked(long ownerId, Map<Long, Entry> next) {
        Map<Long, Entry> old = entries.get(ownerId);
        if (old != null) old.values().forEach(this::unindex);
        if (next.isEmpty()) entries.remove(ownerId);
        else entries.put(ownerId, next);
        next.values().forEach(this::index);
        revision++;
    }

    public void clearOwner(long ownerId) {
        synchronized (lock) {
            Map<Long, Entry> removed = entries.remove(ownerId);
            if (removed == null) return;
            removed.values().forEach(this::unindex);
            revision++;
        }
        notifyListeners();
    }

    public void addListener(Runnable listener) { listeners.add(Objects.requireNonNull(listener)); }
    public void removeListener(Runnable listener) { listeners.remove(listener); }

    /** Runs an action while updates are excluded, if the expected revision is still current. */
    public boolean ifRevision(long expected, Runnable action) {
        Objects.requireNonNull(action);
        synchronized (lock) {
            if (revision != expected) return false;
            action.run();
            return true;
        }
    }

    private void notifyListeners() {
        for (Runnable listener : listeners) {
            try {
                listener.run();
            } catch (RuntimeException e) {
                LOGGER.error("Annotation listener failed", e);
            }
        }
    }

    // Extremely wide ranges stay in a separate set to bound index memory use.
    private void index(Entry entry) {
        int first = entry.start >> PAGE_SHIFT;
        int last = (entry.end - 1) >> PAGE_SHIFT;
        if (last - first + 1 > MAX_INDEXED_PAGES) {
            wideEntries.add(entry);
            return;
        }
        for (int page = first; page <= last; page++)
            addressPages.computeIfAbsent(page, ignored -> new LinkedHashSet<>()).add(entry);
    }

    private void unindex(Entry entry) {
        int first = entry.start >> PAGE_SHIFT;
        int last = (entry.end - 1) >> PAGE_SHIFT;
        if (last - first + 1 > MAX_INDEXED_PAGES) {
            wideEntries.remove(entry);
            return;
        }
        for (int page = first; page <= last; page++) {
            Set<Entry> pageEntries = addressPages.get(page);
            pageEntries.remove(entry);
            if (pageEntries.isEmpty()) addressPages.remove(page);
        }
    }
}
