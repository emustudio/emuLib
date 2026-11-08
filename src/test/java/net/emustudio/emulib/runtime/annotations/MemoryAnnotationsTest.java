/* SPDX-FileCopyrightText: 2006-2026 Peter Jakubčo
   SPDX-License-Identifier: GPL-3.0-or-later */
package net.emustudio.emulib.runtime.annotations;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class MemoryAnnotationsTest {
    private MemoryAnnotations.Entry entry(long owner, long id, int start, int end) {
        return new MemoryAnnotations.Entry(owner, id, start, end, "label", "test", null);
    }

    @Test
    public void replaceOwnerPreservesOtherOwnersAndFindsCoveredAddresses() {
        MemoryAnnotations engine = new MemoryAnnotations();
        MemoryAnnotations.Entry user = entry(1, 1, 10, 12);
        engine.put(user);
        engine.put(entry(2, 1, 10, 11));
        MemoryAnnotations.Entry mapping = entry(2, 2, 11, 14);

        engine.replaceOwner(2, Collections.singletonList(mapping));

        assertSame(user, engine.get(1, 1));
        assertNull(engine.get(2, 1));
        assertEquals(Arrays.asList(user, mapping), engine.at(11));
        assertEquals(Collections.singletonList(mapping), engine.at(13));
        assertTrue(engine.at(14).isEmpty());
    }

    @Test
    public void invalidReplacementDoesNotChangeEntriesOrRevision() {
        MemoryAnnotations engine = new MemoryAnnotations();
        MemoryAnnotations.Entry previous = entry(1, 1, 1, 2);
        engine.put(previous);
        long revision = engine.revision();

        try {
            engine.replaceOwner(1, Collections.singletonList(entry(2, 1, 2, 3)));
            fail("Expected owner validation");
        } catch (IllegalArgumentException expected) {
            assertEquals(revision, engine.revision());
            assertSame(previous, engine.get(1, 1));
        }
    }

    @Test
    public void listenerSeesCurrentStateAndGuardRejectsStaleRevision() {
        MemoryAnnotations engine = new MemoryAnnotations();
        AtomicInteger notifications = new AtomicInteger();
        engine.addListener(() -> {
            assertNotNull(engine.get(1, 1));
            notifications.incrementAndGet();
        });
        long original = engine.revision();

        engine.put(entry(1, 1, 1, 2));

        assertEquals(1, notifications.get());
        assertFalse(engine.ifRevision(original, () -> fail("Stale action executed")));
        AtomicInteger calls = new AtomicInteger();
        assertTrue(engine.ifRevision(engine.revision(), calls::incrementAndGet));
        assertEquals(1, calls.get());
    }

    @Test
    public void compareAndReplaceRejectsStaleRevisionWithoutChangingEntries() {
        MemoryAnnotations engine = new MemoryAnnotations();
        long revision = engine.revision();
        MemoryAnnotations.Entry current = entry(1, 1, 1, 2);
        engine.put(current);

        boolean replaced = engine.compareAndReplaceOwner(revision, 1, Collections.emptyList());

        assertFalse(replaced);
        assertSame(current, engine.get(1, 1));
        assertTrue(engine.compareAndReplaceOwner(engine.revision(), 1, Collections.emptyList()));
        assertTrue(engine.all().isEmpty());
    }

    @Test
    public void listenerFailureDoesNotPreventOtherListenersFromSeeingCommittedChange() {
        MemoryAnnotations engine = new MemoryAnnotations();
        MemoryAnnotations.Entry value = entry(1, 1, 4, 5);
        AtomicInteger notifications = new AtomicInteger();
        engine.addListener(() -> { throw new IllegalStateException("failure"); });
        engine.addListener(() -> {
            assertSame(value, engine.get(1, 1));
            notifications.incrementAndGet();
        });

        engine.put(value);

        assertEquals(1, notifications.get());
        assertSame(value, engine.get(1, 1));
    }

    @Test
    public void overlappingRangesAndWideRangesTrackReplacementsAndRemoval() {
        MemoryAnnotations engine = new MemoryAnnotations();
        MemoryAnnotations.Entry crossingPage = entry(1, 1, 255, 258);
        MemoryAnnotations.Entry overlap = entry(2, 1, 256, 300);
        MemoryAnnotations.Entry wide = entry(3, 1, 0, Integer.MAX_VALUE);
        engine.put(crossingPage);
        engine.put(overlap);
        engine.put(wide);

        assertTrue(engine.at(255).contains(crossingPage));
        assertEquals(3, engine.at(256).size());
        assertEquals(Collections.singletonList(wide), engine.at(300));
        assertEquals(Collections.singletonList(wide), engine.at(Integer.MAX_VALUE - 1));
        assertTrue(engine.at(Integer.MAX_VALUE).isEmpty());

        MemoryAnnotations.Entry moved = entry(1, 1, 700, 701);
        engine.put(moved);
        engine.replaceOwner(2, Collections.singletonList(entry(2, 2, 700, 702)));
        engine.clearOwner(3);

        assertTrue(engine.at(256).isEmpty());
        assertTrue(engine.at(700).contains(moved));
        assertEquals(2, engine.at(700).size());
        engine.remove(1, 1);
        assertEquals(1, engine.at(700).size());
        assertTrue(engine.at(Integer.MAX_VALUE - 1).isEmpty());
    }
}
